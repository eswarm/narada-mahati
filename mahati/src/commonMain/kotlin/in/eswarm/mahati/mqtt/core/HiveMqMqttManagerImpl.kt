package `in`.eswarm.mahati.mqtt.core

import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl
import com.hivemq.client.internal.mqtt.message.auth.MqttSimpleAuth
import `in`.eswarm.mahati.mqtt.common.MqttClientState
import `in`.eswarm.mahati.mqtt.common.MqttMessage
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedContext
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import `in`.eswarm.mahati.db.MqttConnectionParamsEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class HiveMqMqttManagerImpl(
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : MqttManager {

    private var client: Mqtt5AsyncClient? = null
    private var currentParams: MqttConnectionParamsEntity? = null

    private val _connectionState = MutableStateFlow<MqttClientState>(MqttClientState.Disconnected)
    override val connectionState: StateFlow<MqttClientState> = _connectionState.asStateFlow()

    private val _receivedMessages =
        MutableSharedFlow<MqttMessage>(replay = 0, extraBufferCapacity = 64)
    override val receivedMessages: SharedFlow<MqttMessage> = _receivedMessages.asSharedFlow()


    private fun MqttClientConnectedContext.toServerUri(): String {
        return try {
            val host = this.clientConfig.serverHost
            val port = this.clientConfig.serverPort
            val ssl = this.clientConfig.sslConfig != null
            (if (ssl) "ssl" else "tcp") + "://" + host + ":" + port
        } catch (e: Exception) {
            "unknown_server"
        }
    }

    override fun connect(params: MqttConnectionParamsEntity) {
        if (_connectionState.value is MqttClientState.Connected || _connectionState.value is MqttClientState.Connecting) {
            if (currentParams == params && client?.state?.isConnectedOrReconnect == true) {
                // Already connected or connecting with the same parameters
                return
            }
            // If connecting with different params, or if state is connecting but client is not, disconnect first
            client?.disconnect()
        }

        currentParams = params
        _connectionState.value = MqttClientState.Connecting

        var clientBuilder = MqttClient.builder().useMqttVersion5()
            .identifier(params.clientID.ifEmpty { UUID.randomUUID().toString() })
            .serverHost(params.brokerHost).serverPort(params.brokerPort.toInt())
            .addConnectedListener { context ->
                _connectionState.value = MqttClientState.Connected(context.toServerUri())
            }.addDisconnectedListener { context ->
                _connectionState.value = MqttClientState.Error(
                    "Disconnected: ${context.cause.message ?: "Unknown reason"}", context.cause
                )
                // You might want a different state for explicit disconnects vs. errors
                // For example, if context.userInitiated is true, then MqttClientState.Disconnected
            }

        if (params.useSsl == 1L) {
            clientBuilder =
                clientBuilder.sslWithDefaultConfig() // Consider more advanced SSL config if needed
        }
        if (params.username != null) {
            clientBuilder = clientBuilder.simpleAuth(
                MqttSimpleAuth(
                    MqttUtf8StringImpl.of(
                        params.username
                    ), ByteBuffer.wrap(checkNotNull(params.password))
                )
            )
        }

        client = clientBuilder.buildAsync()

        client?.connect()?.whenComplete { connAck, throwable ->
            coroutineScope.launch {
                if (throwable != null) {
                    _connectionState.value = MqttClientState.Error(
                        "Connection failed: ${throwable.message}", throwable
                    )
                } else {
                    if (connAck != null && connAck.reasonCode.isError) {
                        _connectionState.value = MqttClientState.Error(
                            "Connection refused: ${connAck.reasonCode} - ${
                                connAck.reasonString.orElse(MqttUtf8StringImpl.of(""))
                            }", null
                        )
                    } else {
                        // State already set by listener, this is more of a confirmation
                        // _connectionState.value = MqttClientState.Connected (handled by listener)
                        // Setup global publish listener after successful connection
                        client?.publishes(MqttGlobalPublishFilter.ALL) { publish ->
                            val message = MqttMessage(
                                topic = params.topicPrefix + publish.topic.toString(),
                                payload = publish.payloadAsBytes,
                                qos = publish.qos.code,
                                retained = publish.isRetain
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
                    println("Error during disconnect: ${throwable.message}")
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
        if (currentClient == null || currentClient.state?.isConnected != true) {
            return false
        }
        return try {
            val mqttQos = MqttQos.fromCode(qos) ?: MqttQos.AT_MOST_ONCE
            currentClient.publish(
                Mqtt5Publish.builder().topic(currentParams?.topicPrefix ?: ("" + topic))
                    .payload(payload).qos(mqttQos).retain(retain).build()
            ).toSuspend().let { true } // toSuspend will throw on error
        } catch (e: Exception) {
            // Log error
            _connectionState.value = MqttClientState.Error("Publish failed: ${e.message}", e)
            false
        }
    }

    override suspend fun subscribe(topicFilter: String, qos: Int): Boolean {
        val currentClient = client
        if (currentClient == null || currentClient.state?.isConnected != true) {
            return false
        }
        return try {
            val mqttQos = MqttQos.fromCode(qos) ?: MqttQos.AT_MOST_ONCE
            val filter = (currentParams?.topicPrefix ?: "") + topicFilter
            currentClient.subscribeWith()
                .topicFilter(filter).qos(mqttQos).send()
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
        if (currentClient == null || currentClient.state?.isConnected != true) {
            return false
        }
        return try {
            currentClient.unsubscribeWith()
                .topicFilter(currentParams?.topicPrefix ?: ("" + topicFilter)).send().toSuspend()
                .let { unsubAck ->
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
