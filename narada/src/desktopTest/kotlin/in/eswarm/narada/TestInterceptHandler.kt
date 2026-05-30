package `in`.eswarm.narada

import io.moquette.interception.AbstractInterceptHandler
import io.moquette.interception.messages.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Test interceptor that captures broker events for verification.
 * Thread-safe implementation using CopyOnWriteArrayList.
 */
class TestInterceptHandler : AbstractInterceptHandler() {

    private val logs = CopyOnWriteArrayList<String>()

    override fun getID(): String = "TestInterceptHandler"

    override fun onConnect(msg: InterceptConnectMessage?) {
        val logMsg = "Connected: clientID=${msg?.clientID}, username=${msg?.username}"
        logs.add(logMsg)
        println("[BROKER LOG] $logMsg")
    }

    override fun onDisconnect(msg: InterceptDisconnectMessage?) {
        val logMsg = "Disconnected: clientID=${msg?.clientID}, username=${msg?.username}"
        logs.add(logMsg)
        println("[BROKER LOG] $logMsg")
    }

    override fun onConnectionLost(msg: InterceptConnectionLostMessage?) {
        val logMsg = "Connection Lost: clientID=${msg?.clientID}, username=${msg?.username}"
        logs.add(logMsg)
        println("[BROKER LOG] $logMsg")
    }

    override fun onPublish(msg: InterceptPublishMessage) {
        val logMsg = "Published: topic=${msg.topicName}, clientID=${msg.clientID}, username=${msg.username}"
        logs.add(logMsg)
        println("[BROKER LOG] $logMsg")
    }

    override fun onSubscribe(msg: InterceptSubscribeMessage?) {
        val logMsg = "Subscribed: topic=${msg?.topicFilter}, clientID=${msg?.clientID}, username=${msg?.username}"
        logs.add(logMsg)
        println("[BROKER LOG] $logMsg")
    }

    override fun onUnsubscribe(msg: InterceptUnsubscribeMessage?) {
        val logMsg = "Unsubscribed: topic=${msg?.topicFilter}, clientID=${msg?.clientID}, username=${msg?.username}"
        logs.add(logMsg)
        println("[BROKER LOG] $logMsg")
    }

    override fun onMessageAcknowledged(msg: InterceptAcknowledgedMessage?) {
        val logMsg = "Message acknowledged: topic=${msg?.topic}, clientID=${msg?.username}"
        logs.add(logMsg)
        println("[BROKER LOG] $logMsg")
    }

    override fun onSessionLoopError(error: Throwable?) {
        val logMsg = "Session loop error: ${error?.message}"
        logs.add(logMsg)
        println("[BROKER LOG] $logMsg")
    }

    /**
     * Returns a snapshot of all captured logs.
     */
    fun getLogs(): List<String> = logs.toList()

    /**
     * Clears all captured logs.
     */
    fun clear() {
        logs.clear()
    }

    /**
     * Prints all captured logs (useful for debugging).
     */
    fun printLogs() {
        println("=== Captured Broker Logs ===")
        logs.forEach { println("  $it") }
        println("=================================")
    }
}

