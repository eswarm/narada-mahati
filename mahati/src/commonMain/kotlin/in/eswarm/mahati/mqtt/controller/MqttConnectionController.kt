package `in`.eswarm.mahati.mqtt.controller

import `in`.eswarm.mahati.db.AppMqttMessage
import `in`.eswarm.mahati.db.MqttConnection // Still needed to create the object for the manager
import `in`.eswarm.mahati.mqtt.common.MqttClientState
import `in`.eswarm.mahati.mqtt.core.HiveMqttManagerImpl
import `in`.eswarm.mahati.mqtt.core.MqttManager
import `in`.eswarm.mahati.mqtt.service.MqttControllerContract
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MqttConnectionController(
    private val controllerScope: CoroutineScope
) : MqttControllerContract { // Implements the common interface

    private val activeManagers = mutableMapOf<String, MqttManager>()
    private val managerScopes = mutableMapOf<String, CoroutineScope>()
    private val mapMutex = Mutex()

    private val _connectionStatesMap = MutableStateFlow<Map<String, MqttClientState>>(emptyMap())
    override val connectionStatesMap: StateFlow<Map<String, MqttClientState>> =
        _connectionStatesMap.asStateFlow()

    private val _allMessages =
        MutableSharedFlow<Pair<String, AppMqttMessage>>(replay = 0, extraBufferCapacity = 64)
    override val allMessages: SharedFlow<Pair<String, AppMqttMessage>> = _allMessages.asSharedFlow()

    private val commandChannel = Channel<ControllerCommand>(Channel.UNLIMITED)

    sealed class ControllerCommand {
        // Corrected to use MqttConnectionConfig
        data class AddConnection(val config: MqttConnection) : ControllerCommand()
        data class RemoveConnection(val connectionId: String) : ControllerCommand()
        data class PublishMessage(
            val connectionId: String,
            val topic: String,
            val payload: ByteArray,
            val qos: Int,
            val retain: Boolean,
            val result: CompletableDeferred<Boolean> // Added for result callback
        ) : ControllerCommand()

        data class SubscribeToTopic(
            val connectionId: String,
            val topicFilter: String,
            val qos: Int,
            val result: CompletableDeferred<Boolean> // Added for result callback
        ) : ControllerCommand()

        data class UnsubscribeToTopic(
            val connectionId: String,
            val topicFilter: String,
            val result: CompletableDeferred<Boolean> // Added for result callback
        ) : ControllerCommand()

    }

    init {
        controllerScope.launch {
            for (command in commandChannel) {
                when (command) {
                    is ControllerCommand.AddConnection -> handleAddConnection(command.config)
                    is ControllerCommand.RemoveConnection -> handleRemoveConnection(command.connectionId)
                    is ControllerCommand.PublishMessage -> handlePublishMessage(command)
                    is ControllerCommand.SubscribeToTopic -> handleSubscribeToTopic(command)
                    is ControllerCommand.UnsubscribeToTopic -> handleUnsubscribeToTopic(command)

                }
            }
        }
    }

    // Corrected to use MqttConnectionConfig
    private suspend fun handleAddConnection(config: MqttConnection) {
        mapMutex.withLock {
            if (activeManagers.containsKey(config.clientID)) {
                //activeManagers[config.clientID]
            } else {
                val managerJob = SupervisorJob(controllerScope.coroutineContext[Job])
                val newManagerScope =
                    CoroutineScope(controllerScope.coroutineContext + managerJob + CoroutineName("ManagerScope-${config.clientID}"))
                managerScopes[config.clientID] = newManagerScope

                val manager = HiveMqttManagerImpl(newManagerScope)
                activeManagers[config.clientID] = manager

                newManagerScope.launch {
                    manager.connectionState.collect { state ->
                        _connectionStatesMap.update { currentMap ->
                            currentMap.toMutableMap().apply { this[config.clientID] = state }
                        }
                    }
                }
                newManagerScope.launch {
                    manager.receivedMessages.collect { message ->
                        _allMessages.tryEmit(config.clientID to message)
                    }
                }
            }

            val manager = checkNotNull(activeManagers[config.clientID])

            if (manager.connectionState.value !is MqttClientState.Connected) {
                _connectionStatesMap.update { currentMap ->
                    currentMap.toMutableMap()
                        .apply { this[config.clientID] = MqttClientState.Connecting }
                }
                manager.connect(config)
            }
        }
    }

    private suspend fun handleRemoveConnection(connectionId: String) {
        mapMutex.withLock {
            managerScopes.remove(connectionId)?.cancel()
            activeManagers.remove(connectionId)?.apply { cleanup() }
            _connectionStatesMap.update { currentMap ->
                currentMap.toMutableMap().apply { remove(connectionId) }
            }
        }
    }

    private suspend fun handlePublishMessage(command: ControllerCommand.PublishMessage) {
        val manager = mapMutex.withLock { activeManagers[command.connectionId] }
        // Assuming manager.publish is a suspend function that returns Boolean
        val success =
            manager?.publish(command.topic, command.payload, command.qos, command.retain) ?: false
        command.result.complete(success)
    }

    private suspend fun handleSubscribeToTopic(command: ControllerCommand.SubscribeToTopic) {
        val manager = mapMutex.withLock { activeManagers[command.connectionId] }
        // Assuming manager.subscribe is a suspend function that returns Boolean
        val success = manager?.subscribe(command.topicFilter, command.qos) ?: false
        command.result.complete(success)
    }

    private suspend fun handleUnsubscribeToTopic(command: ControllerCommand.UnsubscribeToTopic) {
        val manager = mapMutex.withLock { activeManagers[command.connectionId] }
        // Assuming manager.unsubscribe is a suspend function that returns Boolean
        val success = manager?.unsubscribe(command.topicFilter) ?: false
        command.result.complete(success)
    }

    // Implementation of the MqttControllerContract
    // Corrected to use MqttConnectionConfig
    override fun addConnection(config: MqttConnection) {
        controllerScope.launch { commandChannel.send(ControllerCommand.AddConnection(config)) }
    }

    override fun removeConnection(clientID: String) {
        controllerScope.launch { commandChannel.send(ControllerCommand.RemoveConnection(clientID)) }
    }

    // This is now a suspend function to await the result
    override suspend fun publish(
        clientID: String, topic: String, payload: ByteArray, qos: Int, retain: Boolean
    ): Boolean {
        val deferredResult = CompletableDeferred<Boolean>()
        commandChannel.send(
            ControllerCommand.PublishMessage(
                clientID, topic, payload, qos, retain, deferredResult
            )
        )
        return deferredResult.await()
    }

    // This is now a suspend function to await the result
    override suspend fun subscribe(clientID: String, topicFilter: String, qos: Int): Boolean {
        val deferredResult = CompletableDeferred<Boolean>()
        commandChannel.send(
            ControllerCommand.SubscribeToTopic(
                clientID, topicFilter, qos, deferredResult
            )
        )
        return deferredResult.await()
    }

    override suspend fun unsubscribe(
        clientID: String, topicFilter: String
    ): Boolean {
        val deferredResult = CompletableDeferred<Boolean>()
        commandChannel.send(
            ControllerCommand.UnsubscribeToTopic(
                clientID, topicFilter, deferredResult
            )
        )
        return deferredResult.await()
    }

    override fun shutdownAll() {
        controllerScope.launch {
            mapMutex.withLock {
                managerScopes.values.forEach { it.cancel() }
                managerScopes.clear()
                activeManagers.values.forEach { it.cleanup() }
                activeManagers.clear()
                _connectionStatesMap.value = emptyMap()
            }
        }
    }
}
