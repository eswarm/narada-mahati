package `in`.eswarm.narada.mqtt

import `in`.eswarm.narada.log.LogData
import `in`.eswarm.narada.log.LogStream
import `in`.eswarm.narada.log.log
import io.moquette.BrokerConstants
import io.moquette.broker.Server
import io.moquette.broker.config.IConfig
import io.moquette.broker.config.MemoryConfig
import io.moquette.interception.InterceptHandler
import io.netty.buffer.Unpooled
import io.netty.handler.codec.mqtt.MqttMessageBuilders
import io.netty.handler.codec.mqtt.MqttQoS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.Boolean
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.Exception

class MQTTWrapper(private val listener: MQTTServerListener, private val logStream: LogStream) {

    val TAG = "MQTTWrapper"
    private var mqttBroker: Server? = null
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<kotlin.Boolean> = _isRunning

    // Use a CoroutineScope for structured concurrency, which is safer and more idiomatic in KMP.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val clientsConnected: Int
        get() {
            return try {
                if (!_isRunning.value) return 0
                mqttBroker?.listConnectedClients()?.size ?: 0
            } catch (_: IllegalStateException) {
                0
            }
        }

    fun startMoquette(serverProperties: ServerProperties) {
        if (_isRunning.value) {
            return
        }

        scope.launch {
            try {
                _isRunning.value = true
                logStream.addLog(LogData("Initializing MQTT server..."))

                mqttBroker = Server()
                val userHandlers: List<InterceptHandler> = listOf(listener)
                val memoryConfig = getMemoryConfig(serverProperties)

                // startServer is a blocking call, perfect for Dispatchers.IO
                mqttBroker?.startServer(memoryConfig, userHandlers)

                logStream.addLog(LogData("Server started successfully on port ${serverProperties.mqttPort}."))

                Thread.sleep(5000)

                log(TAG, "Before self publish")
                logStream.addLog(LogData("Before self publish"))
                val message = MqttMessageBuilders.publish()
                    .topicName("/exit")
                    .retained(true)
                    .qos(MqttQoS.EXACTLY_ONCE)
                    .payload(Unpooled.copiedBuffer("Hello World!!".toByteArray(StandardCharsets.UTF_8)))
                    .build()

                mqttBroker?.internalPublish(message, "INTRLPUB")
                log(TAG, "After self publish")
                logStream.addLog(LogData("After self publish"))

            } catch (e: Exception) {
                // If startup fails, reset the state and log the error.
                _isRunning.value = false
                val errorMessage = "Failed to start MQTT server: ${e.message}"
                log(TAG, errorMessage)
                logStream.addLog(LogData(errorMessage))
            }
        }
    }

    fun stopMoquette() {
        if (!_isRunning.value) {
            return
        }

        scope.launch {
            try {
                logStream.addLog(LogData("Stopping MQTT server..."))
                mqttBroker?.stopServer()
                _isRunning.value = false
                logStream.addLog(LogData("Server stopped."))
            } catch (e: Exception) {
                val errorMessage = "Error while stopping MQTT server: ${e.message}"
                log(TAG, errorMessage)
                logStream.addLog(LogData(errorMessage))
                // Even if stop fails, we consider it stopped from the app's perspective.
                _isRunning.value = false
            }
        }
    }

    private fun getMemoryConfig(serverProperties: ServerProperties): MemoryConfig {
        val defaultProperties = Properties()

        defaultProperties[IConfig.PORT_PROPERTY_NAME] =
            serverProperties.mqttPort.toString()
        defaultProperties[IConfig.HOST_PROPERTY_NAME] = BrokerConstants.HOST

        if (serverProperties.wsEnabled) {
            defaultProperties[IConfig.WEB_SOCKET_PORT_PROPERTY_NAME] =
                serverProperties.wsPort.toString()
            defaultProperties[IConfig.WEB_SOCKET_PATH_PROPERTY_NAME] =
                serverProperties.wsPath
        } else {
            defaultProperties[IConfig.WEB_SOCKET_PORT_PROPERTY_NAME] =
                serverProperties.wsPort
            defaultProperties[IConfig.WEB_SOCKET_PATH_PROPERTY_NAME] =
                ""
        }

        if (serverProperties.authEnabled) {
            defaultProperties[IConfig.AUTHENTICATOR_CLASS_NAME] =
                BasicAuthenticator::class.java.canonicalName
            defaultProperties[BasicAuthenticator.USERNAME] = serverProperties.userName
            defaultProperties[BasicAuthenticator.PASSWORD] = serverProperties.password
            defaultProperties[IConfig.ALLOW_ANONYMOUS_PROPERTY_NAME] =
                Boolean.FALSE.toString()
        } else {
            defaultProperties[IConfig.ALLOW_ANONYMOUS_PROPERTY_NAME] =
                Boolean.TRUE.toString()
            defaultProperties[IConfig.AUTHENTICATOR_CLASS_NAME] =
                ""
        }

        defaultProperties[IConfig.PERSISTENCE_ENABLED_PROPERTY_NAME] = Boolean.FALSE.toString()
        return MemoryConfig(defaultProperties)
    }
}