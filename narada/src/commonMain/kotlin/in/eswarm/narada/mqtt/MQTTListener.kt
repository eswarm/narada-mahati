package `in`.eswarm.narada.mqtt

import `in`.eswarm.narada.log.LogData
import `in`.eswarm.narada.log.LogStream
import io.moquette.interception.AbstractInterceptHandler
import io.moquette.interception.messages.*

class MQTTServerListener(private val logStream: LogStream) : AbstractInterceptHandler() {

    override fun getID(): String {
        return "MQTTServerListener"
    }

    override fun onConnect(msg: InterceptConnectMessage?) {
        val logMsg = "Connected ${msg?.username} ${msg?.clientID}"
        log(logMsg)
    }

    override fun onDisconnect(msg: InterceptDisconnectMessage?) {
        val logMsg = "Disconnected ${msg?.username} ${msg?.clientID}"
        log(logMsg)
    }

    override fun onConnectionLost(msg: InterceptConnectionLostMessage?) {
        val logMsg = "Connection Lost for ${msg?.username} ${msg?.clientID}"
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
        val logMsg = "Unsubscribed topic ${msg?.topicFilter} by ${msg?.username} ${msg?.clientID}"
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
        logStream.addLog(LogData(logMsg))
    }

    companion object {
        const val TAG = "MQTTServerListener"
    }
}