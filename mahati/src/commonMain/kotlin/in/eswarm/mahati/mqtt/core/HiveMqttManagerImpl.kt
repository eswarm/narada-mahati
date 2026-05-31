package `in`.eswarm.mahati.mqtt.core

import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl
import com.hivemq.client.internal.mqtt.message.auth.MqttSimpleAuth
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.MqttWebSocketConfig
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnect
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedContext
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import `in`.eswarm.mahati.db.AppMqttMessage
import `in`.eswarm.mahati.db.MessageDirection
import `in`.eswarm.mahati.db.MqttConnectionModel
import `in`.eswarm.mahati.mqtt.common.MqttClientState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class HiveMqttManagerImpl(private val coroutineScope: CoroutineScope) : MqttManager {
    private var client: Mqtt5AsyncClient? = null
    private var currentParams: MqttConnectionModel? = null
    private val _connectionState = MutableStateFlow<MqttClientState>(MqttClientState.Disconnected)
    override val connectionState: StateFlow<MqttClientState> = _connectionState.asStateFlow()
    private val _receivedMessages =
        MutableSharedFlow<AppMqttMessage>(replay = 0, extraBufferCapacity = 64)
    override val receivedMessages: SharedFlow<AppMqttMessage> = _receivedMessages.asSharedFlow()

    override var onReconnected: (suspend () -> Unit)? = null

    private val logger = LoggerFactory.getLogger(HiveMqttManagerImpl::class.java)

    companion object {
        private const val KEEP_ALIVE_SECONDS = 5 * 60 // 5 minutes
        private const val SESSION_EXPIRY_SECONDS = 24 * 60 * 60L // 24 hours
        private const val RECONNECT_INITIAL_DELAY_SECONDS = 30L // 30 seconds
        private const val RECONNECT_MAX_DELAY_SECONDS = 2 * 60L // 2 minutes. 
    }

    private fun Throwable?.isTransientDisconnect(): Boolean {
        val message = this?.message?.lowercase() ?: return false
        return message.contains("without disconnect") ||
                message.contains("timed out") ||
                message.contains("connection reset") ||
                message.contains("broken pipe") ||
                message.contains("connection refused")
    }

    private fun MqttClientConnectedContext.toServerUri(): String {
        return try {
            val host = this.clientConfig.serverHost
            val port = this.clientConfig.serverPort
            val scheme = if (currentParams?.useSsl == true) "ssl" else "tcp"
            "$scheme://$host:$port"
        } catch (_: Exception) {
            "unknown_server"
        }
    }

    override fun connect(params: MqttConnectionModel, autoReconnect: Boolean) {
        if (_connectionState.value is MqttClientState.Connected || _connectionState.value is MqttClientState.Connecting) {
            if (currentParams == params && client?.state?.isConnectedOrReconnect == true) {
                // Already connected or connecting with the same parameters
                return
            }
        }

        // Always forcefully disconnect the previous client to kill any lingering AutoReconnect
        // loops that cause connection ping-pong when retrying after an Error state.
        client?.disconnect()

        currentParams = params
        _connectionState.value = MqttClientState.Connecting

        var clientBuilder = MqttClient.builder().useMqttVersion5()
            .identifier(params.clientID).serverHost(params.brokerHost)
            .serverPort(params.brokerPort.toInt())

        if (autoReconnect) {
            clientBuilder = clientBuilder.automaticReconnect(
                MqttClientAutoReconnect.builder()
                    .initialDelay(RECONNECT_INITIAL_DELAY_SECONDS, TimeUnit.SECONDS)
                    .maxDelay(RECONNECT_MAX_DELAY_SECONDS, TimeUnit.SECONDS)
                    .build()
            )
        }

        clientBuilder = clientBuilder.addConnectedListener { context ->
                logger.info("Connected to ${context.toServerUri()}")
                _connectionState.value =
                    MqttClientState.Connected(context.toServerUri(), params.clientID)
                coroutineScope.launch {
                    onReconnected?.invoke()
                }
            }.addDisconnectedListener { context ->
                val cause = context.cause
                val reason = cause.message ?: "Unknown reason"
                logger.info("Disconnected: $reason")

                _connectionState.value = if (cause.isTransientDisconnect()) {
                    MqttClientState.Connecting
                } else {
                    MqttClientState.Error("Disconnected: $reason", cause)
                }
            }

        // SSL must be configured BEFORE WebSocket for wss:// connections
        if (params.useSsl) {
            clientBuilder =
                clientBuilder.sslWithDefaultConfig() // Consider more advanced SSL config if needed
            logger.info("SSL configured for ${if (params.useWebsocket) "wss://" else "ssl://"}${params.brokerHost}:${params.brokerPort}")
        }

        if (params.username != null) {
            clientBuilder = clientBuilder.simpleAuth(
                MqttSimpleAuth(
                    MqttUtf8StringImpl.of(
                        params.username
                    ), ByteBuffer.wrap(checkNotNull(params.password))
                )
            )
            logger.info("Authentication configured for user: ${params.username}")
        }

        // WebSocket configuration - must come after SSL if using wss://
        if (params.useWebsocket) {
            // Use default path "/mqtt" if not specified, as per MQTT over WebSocket standard
            val wsPath = params.webSocketPath.ifBlank { "/mqtt" }
            logger.info("Configuring WebSocket with path: $wsPath at ${if (params.useSsl) "wss" else "ws"}://${params.brokerHost}:${params.brokerPort}$wsPath")

            clientBuilder = clientBuilder.webSocketConfig(
                MqttWebSocketConfig.builder()
                    .serverPath(wsPath)
                    .build()
            )
        } else {
            logger.info("Configuring ${if (params.useSsl) "SSL" else "TCP"} transport at ${params.brokerHost}:${params.brokerPort}")
        }

        client = clientBuilder.buildAsync()

        client?.connect(
            Mqtt5Connect
                .builder()
                .keepAlive(KEEP_ALIVE_SECONDS)
                .cleanStart(false)
                .willPublish().topic("home").payload("Disconnected".toByteArray())
                .applyWillPublish()
                .sessionExpiryInterval(SESSION_EXPIRY_SECONDS).build()
        )?.whenComplete { connAck, throwable ->
            coroutineScope.launch {
                if (throwable != null) {
                    val errorMsg = when {
                        throwable.message?.contains("ConnectTimeoutException") == true -> {
                            if (params.useWebsocket) {
                                "WebSocket connection timeout - check that:\n" +
                                "1. Broker WebSocket is enabled on port ${params.brokerPort}\n" +
                                "2. WebSocket path is correct (current: ${params.webSocketPath.ifBlank { "/mqtt" }})\n" +
                                "3. Network connectivity is available"
                            } else {
                                "Connection timeout - check network connectivity and broker availability at ${params.brokerHost}:${params.brokerPort}"
                            }
                        }

                        throwable.message?.contains("UnknownHostException") == true ->
                            "Cannot resolve host ${params.brokerHost} - check network and DNS settings"

                        throwable.message?.contains("ConnectionRefusedException") == true -> {
                            if (params.useWebsocket) {
                                "WebSocket connection refused - broker may not have WebSocket enabled or is not running on ${params.brokerHost}:${params.brokerPort}"
                            } else {
                                "Connection refused - broker may not be running on ${params.brokerHost}:${params.brokerPort}"
                            }
                        }

                        throwable.message?.contains("handshake") == true ->
                            "WebSocket handshake failed - check WebSocket path (current: ${params.webSocketPath.ifBlank { "/mqtt" }}) and broker WebSocket configuration"

                        throwable.message?.contains("401") == true || throwable.message?.contains("403") == true ->
                            "Authentication failed - check username and password"

                        throwable.message?.contains("404") == true ->
                            "WebSocket path not found - check that path '${params.webSocketPath.ifBlank { "/mqtt" }}' is correct"

                        else -> "Connection failed: ${throwable.message}"
                    }
                    logger.error(errorMsg, throwable)
                    _connectionState.value = MqttClientState.Error(errorMsg, throwable)
                } else {
                    if (connAck != null && connAck.reasonCode.isError) {
                        _connectionState.value = MqttClientState.Error(
                            "Connection refused: ${connAck.reasonCode} - ${
                                connAck.reasonString.orElse(MqttUtf8StringImpl.of(""))
                            }", null
                        )
                    } else {
                        // Setup global publish listener after successful connection
                        client?.publishes(MqttGlobalPublishFilter.ALL) { publish ->

                            val userProps = publish.userProperties
                            val publisherClientIdFromProps = userProps.asList()
                                .find { it.name.toString() == "clientID" }?.value?.toString() // Convert MqttUtf8String to String

                            val direction =
                                if (publisherClientIdFromProps != null && publisherClientIdFromProps == currentParams?.clientID) {
                                    MessageDirection.SENT
                                } else {
                                    MessageDirection.RECEIVED
                                }

                            val message = AppMqttMessage(
                                topicName = (currentParams?.topicPrefix
                                    ?: "") + publish.topic.toString(),
                                payload = publish.payloadAsBytes,
                                qos = publish.qos.code.toLong(),
                                publisherID = publisherClientIdFromProps ?: "-",
                                connectionID = currentParams?.clientID ?: "-",
                                retained = publish.isRetain,
                                id = 0,
                                direction = direction,
                                timestamp = System.currentTimeMillis()
                            )
                            coroutineScope.launch {
                                _receivedMessages.emit(message)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun disconnect() {
        client?.disconnect()?.whenComplete { _, throwable ->
            coroutineScope.launch {
                _connectionState.value = MqttClientState.Disconnected
                if (throwable != null) {
                    // Log error if disconnect itself failed, though state is Disconnected
                    logger.error("Error during disconnect: ${throwable.message}")
                }
            }
        } ?: run {
            _connectionState.value = MqttClientState.Disconnected // If client was null
        }
        // currentParams = null // debatable if to clear this here or on next connect
    }

    override suspend fun publish(
        topic: String, message: String, qos: Int, retain: Boolean
    ): Boolean {
        return publish(topic, message.toByteArray(StandardCharsets.UTF_8), qos, retain)
    }

    override suspend fun publish(
        topic: String, payload: ByteArray, qos: Int, retain: Boolean
    ): Boolean {
        val currentClient = client
        if (currentClient == null || !currentClient.state.isConnected) {
            return false
        }
        return try {
            val mqttQos = MqttQos.fromCode(qos) ?: MqttQos.AT_MOST_ONCE
            currentClient.publish(
                Mqtt5Publish.builder().topic((currentParams?.topicPrefix ?: "") + topic)
                    .userProperties().add("clientID", currentParams?.clientID ?: "")
                    .applyUserProperties().payload(payload).qos(mqttQos).retain(retain).build()
            ).toSuspend().let { true } // toSuspend will throw on error
        } catch (e: Exception) {
            // Log error
            _connectionState.value = MqttClientState.Error("Publish failed: ${e.message}", e)
            false
        }
    }

    override suspend fun subscribe(topicFilter: String, qos: Int): Boolean {
        val currentClient = client
        if (currentClient == null || !currentClient.state.isConnected) {
            return false
        }
        return try {
            val mqttQos = MqttQos.fromCode(qos) ?: MqttQos.AT_MOST_ONCE
            val filter = (currentParams?.topicPrefix ?: "") + topicFilter
            currentClient.subscribeWith().topicFilter(filter).qos(mqttQos).callback({}).send()
                .toSuspend().let { subAck ->
                    !subAck.reasonCodes.any { it.isError }
                }
        } catch (e: Exception) {
            _connectionState.value = MqttClientState.Error("Subscribe failed: ${e.message}", e)
            false
        }
    }

    override suspend fun unsubscribe(topicFilter: String): Boolean {
        val currentClient = client
        if (currentClient == null || !currentClient.state.isConnected) {
            return false
        }
        return try {
            val filter = (currentParams?.topicPrefix ?: "") + topicFilter
            currentClient.unsubscribeWith().topicFilter(filter).send().toSuspend().let { unsubAck ->
                !unsubAck.reasonCodes.any { it.isError }
            }
        } catch (e: Exception) {
            _connectionState.value = MqttClientState.Error("Unsubscribe failed: ${e.message}", e)
            false
        }
    }

    override fun cleanup() {
        disconnect()
        coroutineScope.cancel() // Cancel the scope to clean up any running coroutines
    }
}

// Helper extension to convert CompletableFuture to suspend function
suspend fun <T> CompletableFuture<T>.toSuspend(): T {
    return suspendCancellableCoroutine { continuation ->
        whenComplete { result, throwable ->
            if (throwable != null) {
                continuation.resumeWithException(throwable)
            } else {
                continuation.resume(result)
            }
        }
        continuation.invokeOnCancellation {
            this.cancel(false)
        }
    }
}
