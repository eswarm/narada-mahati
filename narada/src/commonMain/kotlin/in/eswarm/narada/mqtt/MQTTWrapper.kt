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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.Boolean
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.Exception
import kotlin.Int

class MQTTWrapper(private val listener: MQTTServerListener, private val logStream: LogStream) {

    val TAG = "MQTTWrapper"
    private var mqttBroker: Server? = null
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<kotlin.Boolean> = _isRunning

    var threadExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    val clientsConnected: Int
        get() {
            return try {
                if(!_isRunning.value) return 0
                mqttBroker?.listConnectedClients()?.size ?: 0
            } catch (_: IllegalStateException) {
                0
            }
        }

    fun startMoquette(
        serverProperties: ServerProperties
    ) {
        if (_isRunning.value) {
            return
        }
        _isRunning.value = true

        threadExecutor.execute {
            mqttBroker = Server()
            val userHandlers: List<InterceptHandler?> = listOf(listener)
            logStream.addLog(LogData("Init server."))
            mqttBroker?.startServer(getMemoryConfig(serverProperties), userHandlers)
            logStream.addLog(LogData("Starting Server"))

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
        }
    }

    fun stopMoquette() {
        if (!_isRunning.value) {
            return
        }
        _isRunning.value = false

        threadExecutor.execute {
            try {
                mqttBroker?.stopServer()
            } catch (e: Exception) {
                log(TAG, e.message ?: "")
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