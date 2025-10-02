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
        // Uses the common MqttConnectionConfig now
        data class AddConnection(val config: MqttConnection) : ControllerCommand()
        data class RemoveConnection(val connectionId: String) : ControllerCommand()
        data class PublishMessage(
            val connectionId: String,
            val topic: String,
            val payload: ByteArray,
            val qos: Int,
            val retain: Boolean
        ) : ControllerCommand()

        data class SubscribeToTopic(
            val connectionId: String, val topicFilter: String, val qos: Int
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
                }
            }
        }
    }

    private suspend fun handleAddConnection(config: MqttConnection) {
        mapMutex.withLock {
            if (activeManagers.containsKey(config.clientID)) {
                // Log or handle re-connection attempt
                return@withLock
            }

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

            _connectionStatesMap.update { currentMap ->
                currentMap.toMutableMap()
                    .apply { this[config.clientID] = MqttClientState.Connecting }
            }

            manager.connect(config)
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
        manager?.publish(command.topic, command.payload, command.qos, command.retain)
    }

    private suspend fun handleSubscribeToTopic(command: ControllerCommand.SubscribeToTopic) {
        val manager = mapMutex.withLock { activeManagers[command.connectionId] }
        manager?.subscribe(command.topicFilter, command.qos)
    }

    // Implementation of the MqttControllerContract
    override fun addConnection(config: MqttConnection) {
        controllerScope.launch { commandChannel.send(ControllerCommand.AddConnection(config)) }
    }

    override fun removeConnection(connectionId: String) {
        controllerScope.launch { commandChannel.send(ControllerCommand.RemoveConnection(connectionId)) }
    }

    override fun publish(
        connectionId: String, topic: String, payload: ByteArray, qos: Int, retain: Boolean
    ) {
        controllerScope.launch {
            commandChannel.send(
                ControllerCommand.PublishMessage(
                    connectionId, topic, payload, qos, retain
                )
            )
        }
    }

    override fun subscribe(connectionId: String, topicFilter: String, qos: Int) {
        controllerScope.launch {
            commandChannel.send(
                ControllerCommand.SubscribeToTopic(
                    connectionId, topicFilter, qos
                )
            )
        }
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
