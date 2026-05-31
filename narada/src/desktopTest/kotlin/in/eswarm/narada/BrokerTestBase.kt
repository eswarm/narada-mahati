package `in`.eswarm.narada

import io.moquette.broker.Server
import io.moquette.broker.config.MemoryConfig
import io.moquette.interception.InterceptHandler
import org.junit.After
import org.junit.Before
import java.util.Properties
import kotlin.test.fail

/**
 * Base class for broker integration tests.
 * Provides reusable broker setup/teardown and configuration helpers.
 */
abstract class BrokerTestBase {

    protected var mqttBroker: Server? = null
    protected val brokerHost = "127.0.0.1"
    protected val brokerPort = 1883
    protected val brokerStartupWaitMs = 3000L
    protected val testTimeoutMs = 30000L
    
    protected val testInterceptHandler = TestInterceptHandler()

    @Before
    open fun setUp() {
        println("=== Test Setup ===")
        testInterceptHandler.clear()
    }

    @After
    open fun tearDown() {
        println("=== Test Teardown ===")
        try {
            mqttBroker?.stopServer()
            println("Broker stopped successfully")
        } catch (e: Exception) {
            println("Warning: Error stopping broker: ${e.message}")
        }
        // Give a moment for port to be released
        Thread.sleep(1000)
    }

    /**
     * Creates and starts a broker with the given configuration.
     */
    protected fun createBroker(config: Properties, handlers: List<InterceptHandler> = listOf(testInterceptHandler)) {
        try {
            mqttBroker = Server()
            val memoryConfig = MemoryConfig(config)
            mqttBroker?.startServer(memoryConfig, handlers)
            println("Broker started successfully")
            Thread.sleep(brokerStartupWaitMs)
        } catch (e: Exception) {
            e.printStackTrace()
            fail("Failed to start MQTT broker: ${e.message}")
        }
    }

    /**
     * Creates default anonymous broker configuration.
     */
    protected fun createDefaultConfig(): Properties {
        val config = Properties()
        config.setProperty("host", brokerHost)
        config.setProperty("port", brokerPort.toString())
        config.setProperty("allow_anonymous", "true")
        config.setProperty("persistence_enabled", "false")
        return config
    }

    /**
     * Creates broker configuration with authentication enabled.
     */
    protected fun createAuthConfig(username: String, password: String): Properties {
        val config = Properties()
        config.setProperty("host", brokerHost)
        config.setProperty("port", brokerPort.toString())
        config.setProperty("allow_anonymous", "false")
        config.setProperty("persistence_enabled", "false")
        config.setProperty("authenticator_class", "in.eswarm.narada.mqtt.BasicAuthenticator")
        config.setProperty("narada_username", username)
        config.setProperty("narada_password", password)
        return config
    }

    /**
     * Creates broker configuration with WebSocket enabled.
     */
    protected fun createWebSocketConfig(wsPort: Int = 8080, wsPath: String = "/mqtt"): Properties {
        val config = createDefaultConfig()
        config.setProperty("websocket_port", wsPort.toString())
        config.setProperty("websocket_path", wsPath)
        return config
    }

    /**
     * Verifies that the broker log contains the expected message.
     * Polls for up to 5 seconds to account for async logging.
     */
    protected fun assertLogContains(expected: String, message: String = "Log should contain: $expected") {
        val maxAttempts = 50  // 5 seconds total
        var found = false
        
        for (attempt in 1..maxAttempts) {
            val logs = testInterceptHandler.getLogs()
            found = logs.any { it.contains(expected, ignoreCase = true) }
            if (found) {
                break
            }
            Thread.sleep(100)  // Wait 100ms between checks
        }
        
        if (!found) {
            val logs = testInterceptHandler.getLogs()
            println("Expected log not found. All logs:")
            logs.forEach { println("  - $it") }
            fail(message)
        }
    }

    /**
     * Gets the count of specific log entries.
     */
    protected fun countLogsContaining(text: String): Int {
        return testInterceptHandler.getLogs().count { it.contains(text, ignoreCase = true) }
    }
}


