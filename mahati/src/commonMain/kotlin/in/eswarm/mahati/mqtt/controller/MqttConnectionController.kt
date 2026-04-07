package `in`.eswarm.mahati.mqtt.controller

import `in`.eswarm.mahati.chat.ChatScreenLifecycle
import `in`.eswarm.mahati.db.AppMqttMessage
import `in`.eswarm.mahati.db.MessageRepository
import `in`.eswarm.mahati.db.MqttConnectionModel
import `in`.eswarm.mahati.db.SubscriptionRepository
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
    private val subscriptionRepo: SubscriptionRepository,
    private val sendNotification: ((String, String, String, String) -> Unit)? = null,
    private val onConnectionAdded: (() -> Unit)? = null
) : MqttControllerContract { // Implements the common interface

    companion object {
        private const val DEFAULT_HOME_TOPIC = "home"
        private const val DEFAULT_HOME_QOS = 1L
    }

    data class ManagerScope(val scope: CoroutineScope, val manager: MqttManager)

    private val activeManagers = mutableMapOf<String, ManagerScope>()
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
        data class AddConnection(val config: MqttConnectionModel) : ControllerCommand()
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

                if (ChatScreenLifecycle.shouldShowNotification(clientID, message.topicName)) {
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

    private suspend fun ensureDefaultHomeSubscription(clientId: String) {
        subscriptionRepo.insertSubscription(
            clientID = clientId,
            topicFilter = DEFAULT_HOME_TOPIC,
            qos = DEFAULT_HOME_QOS,
            subscribedAt = System.currentTimeMillis()
        )
    }

    private suspend fun restoreSubscriptions(clientId: String, mqttManager: MqttManager) {
        // Keep default subscription policy in one place and idempotent via INSERT OR IGNORE.
        ensureDefaultHomeSubscription(clientId)
        val savedSubscriptions = subscriptionRepo.getSubscriptionsByClientId(clientId)
        savedSubscriptions.forEach { sub ->
            mqttManager.subscribe(sub.topicFilter, sub.qos.toInt())
        }
    }

    private suspend fun handleAddConnection(config: MqttConnectionModel) {
        mapMutex.withLock {
            var managerWithScope = activeManagers[config.clientID]

            if (managerWithScope == null) {
                val managerJob = SupervisorJob(controllerScope.coroutineContext[Job])
                val managerScope =
                    CoroutineScope(controllerScope.coroutineContext + managerJob + CoroutineName("ManagerScope-${config.clientID}"))

                val mqttManager = HiveMqttManagerImpl(managerScope)
                activeManagers[config.clientID] = ManagerScope(managerScope, mqttManager)
                managerWithScope = activeManagers[config.clientID]

                // Only attach the collectors ONCE when the manager is first created.
                // Doing this outside the block was causing duplicate message collection.
                managerScope.launch {
                    mqttManager.connectionState.collect { state ->
                        _connectionStatesMap.update { currentMap ->
                            currentMap.toMutableMap().apply { this[config.clientID] = state }
                        }
                    }
                }

                managerScope.launch {
                    mqttManager.receivedMessages.collect { message ->
                        _allMessages.tryEmit(config.clientID to message)
                    }
                }
            }

            val mqttManager = checkNotNull(managerWithScope).manager

            mqttManager.onReconnected = {
                restoreSubscriptions(config.clientID, mqttManager)
            }

            mqttManager.connect(config)
        }
    }

    private suspend fun handleRemoveConnection(connectionId: String) {
        val managerWithScope = mapMutex.withLock {
            val removed = activeManagers.remove(connectionId)
            _connectionStatesMap.update { currentMap ->
                currentMap.toMutableMap().apply { remove(connectionId) }
            }
            removed
        }

        managerWithScope?.apply {
            manager.disconnect()
            delay(1000) // allow network packets to be sent before cancelling
            scope.cancel()
        }
    }

    private suspend fun handlePublishMessage(command: ControllerCommand.PublishMessage) {
        val manager = mapMutex.withLock { activeManagers[command.connectionId]?.manager }
        val success =
            manager?.publish(command.topic, command.payload, command.qos, command.retain) ?: false
        command.result.complete(success)
    }

    private suspend fun handleSubscribeToTopic(command: ControllerCommand.SubscribeToTopic) {
        val manager = mapMutex.withLock { activeManagers[command.connectionId]?.manager }
        // Assuming manager.subscribe is a suspend function that returns Boolean
        val success = manager?.subscribe(command.topicFilter, command.qos) ?: false
        if (success) {
            subscriptionRepo.insertSubscription(
                command.connectionId,
                command.topicFilter,
                command.qos.toLong(),
                System.currentTimeMillis()
            )
        }
        command.result.complete(success)
    }

    private suspend fun handleUnsubscribeToTopic(command: ControllerCommand.UnsubscribeToTopic) {
        val manager = mapMutex.withLock { activeManagers[command.connectionId]?.manager }
        // Assuming manager.unsubscribe is a suspend function that returns Boolean
        val success = manager?.unsubscribe(command.topicFilter) ?: false
        if (success) {
            subscriptionRepo.deleteSubscription(command.connectionId, command.topicFilter)
        }
        command.result.complete(success)
    }

    override fun addConnection(config: MqttConnectionModel) {
        onConnectionAdded?.invoke()
        controllerScope.launch { commandChannel.send(ControllerCommand.AddConnection(config)) }
    }

    override fun removeConnection(clientID: String) {
        controllerScope.launch { commandChannel.send(ControllerCommand.RemoveConnection(clientID)) }
    }

    override fun removeAllConnections() {
        controllerScope.launch {
            val removedManagers = mapMutex.withLock {
                val managers = activeManagers.values.toList()
                activeManagers.clear()
                _connectionStatesMap.value = emptyMap()
                managers
            }
            removedManagers.forEach {
                it.manager.disconnect()
            }
            delay(1000)
            removedManagers.forEach {
                it.scope.cancel()
            }
        }
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
            val removedManagers = mapMutex.withLock {
                val managers = activeManagers.values.toList()
                activeManagers.clear()
                _connectionStatesMap.value = emptyMap()
                managers
            }
            removedManagers.forEach {
                it.manager.disconnect()
            }
            delay(1000)
            removedManagers.forEach {
                it.scope.cancel()
            }
        }
    }
}
