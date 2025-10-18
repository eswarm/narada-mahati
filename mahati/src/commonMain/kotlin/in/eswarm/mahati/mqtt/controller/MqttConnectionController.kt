package `in`.eswarm.mahati.mqtt.controller

import `in`.eswarm.mahati.chat.ChatScreenLifecycle
import `in`.eswarm.mahati.db.AppMqttMessage
import `in`.eswarm.mahati.db.MessageRepository
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
    private val controllerScope: CoroutineScope,
    private val messageRepo: MessageRepository,
    private val sendNotification: ((String, String, String, String) -> Unit)? = null
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

        launchPersistMessage()
    }

    private fun launchPersistMessage() {
        controllerScope.launch {
            allMessages.collect { messagePair ->
                val clientID = messagePair.first
                val message = messagePair.second
                messageRepo.insertMessage(
                    message.connectionID,
                    message.publisherID,
                    message.topicName,
                    message.payload,
                    message.qos,
                    message.retained,
                    message.direction,
                    message.timestamp
                )

                if (!ChatScreenLifecycle.isChatScreenVisible.value) {
                    sendNotification?.invoke(
                        "New message on ${message.topicName}",
                        String(message.payload),
                        clientID,
                        message.topicName
                    )
                }
            }
        }
    }

    private suspend fun handleAddConnection(config: MqttConnection) {
        mapMutex.withLock {
            val existingManager = activeManagers[config.clientID]
            if (existingManager != null) {
                val currentState = existingManager.connectionState.value

                _connectionStatesMap.value = _connectionStatesMap.value.toMutableMap().apply {
                    this[config.clientID] = currentState
                }

                if (currentState !is MqttClientState.Connected) {
                    // It's not connected, so let's try connecting again.
                    _connectionStatesMap.update { currentMap ->
                        currentMap.toMutableMap()
                            .apply { this[config.clientID] = MqttClientState.Connecting }
                    }
                    existingManager.connect(config)
                }
                // If it IS connected, we do nothing more, the state has been emitted.
                return@withLock
            }

            // Manager doesn't exist, create it.
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

            // And connect it for the first time.
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

    override fun addConnection(config: MqttConnection) {
        controllerScope.launch { commandChannel.send(ControllerCommand.AddConnection(config)) }
    }

    override fun removeConnection(clientID: String) {
        controllerScope.launch { commandChannel.send(ControllerCommand.RemoveConnection(clientID)) }
    }

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
