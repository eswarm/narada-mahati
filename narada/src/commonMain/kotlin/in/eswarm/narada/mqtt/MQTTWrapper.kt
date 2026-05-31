package `in`.eswarm.narada.mqtt

import `in`.eswarm.shared.LogData
import `in`.eswarm.shared.LogStream
import io.moquette.BrokerConstants
import io.moquette.broker.Server
import io.moquette.broker.config.IConfig
import io.moquette.broker.config.MemoryConfig
import io.moquette.interception.AbstractInterceptHandler
import io.moquette.interception.InterceptHandler
import io.moquette.interception.messages.InterceptAcknowledgedMessage
import io.moquette.interception.messages.InterceptConnectMessage
import io.moquette.interception.messages.InterceptConnectionLostMessage
import io.moquette.interception.messages.InterceptDisconnectMessage
import io.moquette.interception.messages.InterceptPublishMessage
import io.moquette.interception.messages.InterceptSubscribeMessage
import io.moquette.interception.messages.InterceptUnsubscribeMessage
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
import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.logging.Level
import kotlin.Exception

class MQTTWrapper(private val logStream: LogStream) {

    val TAG = "MQTTWrapper"

    private var server: Server? = null
    private val _isRunning = MutableStateFlow(false)
    val _clientsConnected = MutableStateFlow(0)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val clientsConnected: StateFlow<Int> = _clientsConnected
    val isRunning: StateFlow<kotlin.Boolean> = _isRunning
    var lastMessage: String = ""

    fun startMoquette(serverProperties: ServerProperties) {
        if (_isRunning.value) {
            return
        }

        try {
            val moquetteLogger = Logger.getLogger("io.moquette")
            moquetteLogger.level = Level.ALL
            // Remove existing handlers to avoid duplicates if started multiple times
            moquetteLogger.handlers.forEach { moquetteLogger.removeHandler(it) }
            moquetteLogger.addHandler(object : Handler() {
                override fun publish(record: LogRecord?) {
                    if (record != null) {
                        val simpleName = record.loggerName?.substringAfterLast('.') ?: "Moquette"
                        // Filter repeat messages.
                        if (lastMessage != record.message) {
                            logStream.addLog(
                                LogData(
                                    tag = "MQ-$simpleName",
                                    msg = record.message ?: ""
                                )
                            )
                            lastMessage = record.message ?: ""
                        }
                    }
                }

                override fun flush() {}
                override fun close() {}
            })
        } catch (e: Exception) {
            logStream.addLog(
                LogData(
                    tag = TAG,
                    msg = "Failed to setup moquette logger: ${e.message}"
                )
            )
        }

        scope.launch {
            try {
                _isRunning.value = true
                logStream.addLog(LogData(tag = TAG, msg = "Initializing MQTT server..."))

                server = Server()
                val userHandlers: List<InterceptHandler> = listOf(listener)
                val memoryConfig = getMemoryConfig(serverProperties)

                // startServer is a blocking call, perfect for Dispatchers.IO
                server?.startServer(memoryConfig, userHandlers)

                logStream.addLog(
                    LogData(
                        tag = TAG,
                        "Server started successfully on port ${serverProperties.mqttPort}."
                    )
                )

                Thread.sleep(5000)

                logStream.addLog(LogData(tag = TAG, "Before self publish"))
                val message = MqttMessageBuilders.publish().topicName("/exit").retained(true)
                    .qos(MqttQoS.EXACTLY_ONCE)
                    .payload(Unpooled.copiedBuffer("Hello World!!".toByteArray(StandardCharsets.UTF_8)))
                    .build()

                server?.internalPublish(message, "INTRLPUB")
                logStream.addLog(LogData(tag = TAG, "After self publish"))

            } catch (e: Exception) {
                // If startup fails, reset the state and log the error.
                _isRunning.value = false
                val errorMessage = "Failed to start MQTT server: ${e.message}"
                logStream.addLog(LogData(tag = TAG, errorMessage))
            }
        }
    }

    fun stopMoquette() {
        if (!_isRunning.value) {
            return
        }

        scope.launch {
            try {
                logStream.addLog(LogData(tag = TAG, "Stopping MQTT server..."))
                server?.stopServer()
                _isRunning.value = false
                logStream.addLog(LogData(tag = TAG, "Server stopped."))
            } catch (e: Exception) {
                val errorMessage = "Error while stopping MQTT server: ${e.message}"
                logStream.addLog(LogData(tag = TAG, errorMessage))
                // Even if stop fails, we consider it stopped from the app's perspective.
                _isRunning.value = false
            }
        }
    }

    private fun getMemoryConfig(serverProperties: ServerProperties): MemoryConfig {
        val defaultProperties = Properties()

        defaultProperties[IConfig.PORT_PROPERTY_NAME] = serverProperties.mqttPort.toString()
        defaultProperties[IConfig.HOST_PROPERTY_NAME] = BrokerConstants.HOST

        if (serverProperties.wsEnabled) {
            defaultProperties[IConfig.WEB_SOCKET_PORT_PROPERTY_NAME] =
                serverProperties.wsPort.toString()
            defaultProperties[IConfig.WEB_SOCKET_PATH_PROPERTY_NAME] = serverProperties.wsPath
        } else {
            defaultProperties[IConfig.WEB_SOCKET_PORT_PROPERTY_NAME] = serverProperties.wsPort
            defaultProperties[IConfig.WEB_SOCKET_PATH_PROPERTY_NAME] = ""
        }

        if (serverProperties.authEnabled) {
            defaultProperties[IConfig.AUTHENTICATOR_CLASS_NAME] =
                BasicAuthenticator::class.java.canonicalName
            defaultProperties[BasicAuthenticator.USERNAME] = serverProperties.userName
            defaultProperties[BasicAuthenticator.PASSWORD] = serverProperties.password
            defaultProperties[IConfig.ALLOW_ANONYMOUS_PROPERTY_NAME] = Boolean.FALSE.toString()
        } else {
            defaultProperties[IConfig.ALLOW_ANONYMOUS_PROPERTY_NAME] = Boolean.TRUE.toString()
            defaultProperties[IConfig.AUTHENTICATOR_CLASS_NAME] = ""
        }

        defaultProperties[IConfig.PERSISTENCE_ENABLED_PROPERTY_NAME] = Boolean.FALSE.toString()
        return MemoryConfig(defaultProperties)
    }

    private val listener = object : AbstractInterceptHandler() {

        override fun getID(): String {
            return "MQTTServerListener"
        }

        override fun onConnect(msg: InterceptConnectMessage?) {
            val logMsg = "Connected ${msg?.username} ${msg?.clientID}"
            _clientsConnected.value = server?.listConnectedClients()?.size ?: 0
            log(logMsg)
        }

        override fun onDisconnect(msg: InterceptDisconnectMessage?) {
            val logMsg = "Disconnected ${msg?.username} ${msg?.clientID}"
            _clientsConnected.value = server?.listConnectedClients()?.size ?: 0
            log(logMsg)
        }

        override fun onConnectionLost(msg: InterceptConnectionLostMessage?) {
            val logMsg = "Connection Lost for ${msg?.username} ${msg?.clientID}"
            _clientsConnected.value = server?.listConnectedClients()?.size ?: 0
            log(logMsg)
        }

        override fun onPublish(msg: InterceptPublishMessage) {
            val logMsg =
                "Published message on topic ${msg.topicName} by ${msg.username} ${msg.clientID}"
            log(logMsg)
        }

        override fun onSubscribe(msg: InterceptSubscribeMessage?) {
            val logMsg = "Subscribed topic ${msg?.topicFilter} by ${msg?.username} ${msg?.clientID}"
            log(logMsg)
        }

        override fun onUnsubscribe(msg: InterceptUnsubscribeMessage?) {
            val logMsg =
                "Unsubscribed topic ${msg?.topicFilter} by ${msg?.username} ${msg?.clientID}"
            log(logMsg)
        }

        override fun onMessageAcknowledged(msg: InterceptAcknowledgedMessage?) {
            val logMsg = "Message acknowledged :: ${msg?.username} ${msg?.topic}"
            log(logMsg)
        }

        override fun onSessionLoopError(error: Throwable?) {
            val logMsg = "onSessionLoopError ${error?.message}"
            log(logMsg)
        }

        private fun log(logMsg: String) {
            logStream.addLog(LogData(TAG_LISTENER, logMsg))
        }

        val TAG_LISTENER = "MQTTServerListener"
    }

}