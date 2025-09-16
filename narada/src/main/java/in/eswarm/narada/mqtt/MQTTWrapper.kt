package `in`.eswarm.narada.mqtt

import `in`.eswarm.narada.log.LogData
import `in`.eswarm.narada.log.LogStream
import android.util.Log
import io.moquette.BrokerConstants
import io.moquette.broker.Server
import io.moquette.broker.config.IConfig
import io.moquette.broker.config.MemoryConfig
import io.moquette.interception.InterceptHandler
import io.netty.buffer.Unpooled
import io.netty.handler.codec.mqtt.MqttMessageBuilders
import io.netty.handler.codec.mqtt.MqttQoS
import java.lang.Boolean
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.Exception
import kotlin.Int

object MQTTWrapper {

    const val TAG = "MQTTInitializer"
    private var mqttBroker: Server? = null

    val clientsConnected: Int
        get() {
            return try {
                mqttBroker?.listConnectedClients()?.size ?: 0
            } catch (e: IllegalStateException) {
                0
            }
        }

    fun startMoquette(
        listener: MQTTServerListener,
        logStream: LogStream,
        serverProperties: ServerProperties
    ) {
        mqttBroker = Server()
        val userHandlers: List<InterceptHandler?> = listOf(listener)
        logStream.addLog(LogData("Init server."))
        mqttBroker?.startServer(getMemoryConfig(serverProperties), userHandlers)
        logStream.addLog(LogData("Starting Server"))

        Thread.sleep(20000)

        Log.i(TAG, "Before self publish")
        logStream.addLog(LogData("Before self publish"))
        val message = MqttMessageBuilders.publish()
            .topicName("/exit")
            .retained(true) //        qos(MqttQoS.AT_MOST_ONCE);
            //        qQos(MqttQoS.AT_LEAST_ONCE);
            .qos(MqttQoS.EXACTLY_ONCE)
            .payload(Unpooled.copiedBuffer("Hello World!!".toByteArray(StandardCharsets.UTF_8)))
            .build()

        mqttBroker?.internalPublish(message, "INTRLPUB")
        Log.i(TAG, "After self publish")
        logStream.addLog(LogData("After self publish"))
    }

    fun stopMoquette() {
        try {
            mqttBroker?.stopServer()
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "")
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