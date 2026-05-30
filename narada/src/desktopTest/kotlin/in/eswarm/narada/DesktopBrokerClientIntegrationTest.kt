package `in`.eswarm.narada

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue

/**
 * Desktop Integration Test for Narada (Broker) and MQTT Clients.
 *
 * This is the original smoke test that verifies basic broker-client communication.
 * For comprehensive test coverage of all CSV test cases, see:
 * - NaradaBrokerTests.kt (TC-N1 through TC-N10)
 * - MahatiClientTests.kt (TC-M2 through TC-M7)
 */
class DesktopBrokerClientIntegrationTest : BrokerTestBase() {

    @Before
    override fun setUp() {
        super.setUp()
        println("=== Desktop Integration Test Setup ===")
        println("Starting Moquette MQTT Broker on $brokerHost:$brokerPort...")

        // Create and start broker with default config
        val config = createDefaultConfig()
        createBroker(config)
    }

    @Test
    fun testDesktopBrokerClientCommunication() {
        println("Starting Desktop Integration Test...")

        // Step 1: Verify broker is accessible
        println("Step 1: Verifying broker is accessible...")
        verifyBrokerIsRunning()

        // Step 2: Test MQTT publish/subscribe communication
        println("Step 2: Testing MQTT communication...")
        testMqttCommunication()

        println("✓ Desktop Integration Test completed successfully!")
    }

    private fun verifyBrokerIsRunning() {
        var connected = false
        var attempts = 0
        val maxAttempts = 10

        while (!connected && attempts < maxAttempts) {
            try {
                val client: Mqtt3BlockingClient = MqttClient.builder()
                    .useMqttVersion3()
                    .identifier("integration-test-verify")
                    .serverHost(brokerHost)
                    .serverPort(brokerPort)
                    .buildBlocking()

                client.connect()
                println("✓ Successfully connected to broker")
                client.disconnect()
                connected = true

            } catch (e: Exception) {
                attempts++
                println("Attempt $attempts/$maxAttempts: Broker not ready yet (${e.message})")
                Thread.sleep(2000)
            }
        }

        assertTrue(connected, "Failed to connect to broker after $maxAttempts attempts")
    }

    private fun testMqttCommunication() {
        val testTopic = "test/integration/desktop"
        val testMessage = "Desktop Integration Test Message"
        val messageReceived = CountDownLatch(1)
        var receivedMessage: String? = null

        // Create subscriber client using async API for callbacks
        val subscriberClient = MqttClient.builder()
            .useMqttVersion3()
            .identifier("integration-test-subscriber")
            .serverHost(brokerHost)
            .serverPort(brokerPort)
            .buildAsync()

        // Create publisher client
        val publisherClient: Mqtt3BlockingClient = MqttClient.builder()
            .useMqttVersion3()
            .identifier("integration-test-publisher")
            .serverHost(brokerHost)
            .serverPort(brokerPort)
            .buildBlocking()

        try {
            // Connect subscriber
            println("Connecting subscriber...")
            subscriberClient.connect().get(10, TimeUnit.SECONDS)

            // Subscribe to topic with callback
            println("Subscribing to topic: $testTopic")
            subscriberClient.subscribeWith()
                .topicFilter(testTopic)
                .callback { publish ->
                    val payload = publish.payload.orElse(null)
                    receivedMessage = if (payload != null) {
                        // Extract bytes from ByteBuffer (works with read-only buffers)
                        val bytes = ByteArray(payload.remaining())
                        payload.duplicate().get(bytes)
                        String(bytes)
                    } else {
                        ""
                    }
                    println("✓ Received message: $receivedMessage")
                    messageReceived.countDown()
                }
                .send()
                .get(5, TimeUnit.SECONDS)

            // Connect publisher
            println("Connecting publisher...")
            publisherClient.connect()

            // Publish message
            println("Publishing message: $testMessage")
            publisherClient.publishWith()
                .topic(testTopic)
                .payload(testMessage.toByteArray())
                .send()

            println("Waiting for message to be received...")
            val received = messageReceived.await(testTimeoutMs, TimeUnit.MILLISECONDS)

            assertTrue(received, "Timeout waiting for message")
            assertTrue(receivedMessage == testMessage,
                "Message mismatch. Expected: '$testMessage', Got: '$receivedMessage'")

            println("✓ Message successfully published and received")

        } finally {
            // Cleanup
            try {
                publisherClient.disconnect()
            } catch (e: Exception) {
                println("Warning: Failed to disconnect publisher: ${e.message}")
            }

            try {
                subscriberClient.disconnect().get(5, TimeUnit.SECONDS)
            } catch (e: Exception) {
                println("Warning: Failed to disconnect subscriber: ${e.message}")
            }
        }
    }

}









