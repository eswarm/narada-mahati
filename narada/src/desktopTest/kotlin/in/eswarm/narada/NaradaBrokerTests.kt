package `in`.eswarm.narada

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Desktop integration tests for Narada MQTT Broker functionality.
 *
 * Maps to CSV test cases:
 * - TC-N1: Start broker service
 * - TC-N2: Stop broker service
 * - TC-N3: Client connects to broker
 * - TC-N4: Publish/subscribe loopback
 * - TC-N5: Broker logs stream
 * - TC-N6: Broker stability with multiple clients
 * - TC-N7: Restart broker while clients connected
 * - TC-N9: Auth enabled with wrong credentials
 * - TC-N10: WebSocket mode
 */
class NaradaBrokerTests : BrokerTestBase() {

    /**
     * TC-N1 & TC-N2: Test broker start and stop lifecycle.
     */
    @Test
    fun testBrokerStartStop() {
        println("\n=== TC-N1 & TC-N2: Broker Start/Stop ===")

        // TC-N1: Start broker
        val config = createDefaultConfig()
        createBroker(config)

        // Verify broker is running by attempting connection
        val client = createClient("test-start-stop")
        client.connect()
        assertTrue(client.state.isConnected, "Broker should be running and accept connections")
        println("✓ TC-N1 PASSED: Broker started successfully")

        client.disconnect()

        // TC-N2: Stop broker
        mqttBroker?.stopServer()
        Thread.sleep(1000)

        // Verify broker is stopped
        try {
            val testClient = createClient("test-stopped")
            testClient.connect()
            fail("Should not be able to connect to stopped broker")
        } catch (e: Exception) {
            println("✓ TC-N2 PASSED: Broker stopped successfully (connection refused)")
        }
    }

    /**
     * TC-N3: Test client connects to broker.
     */
    @Test
    fun testClientConnectsToBroker() {
        println("\n=== TC-N3: Client Connects to Broker ===")

        val config = createDefaultConfig()
        createBroker(config)

        // Connect multiple clients with different IDs
        val client1 = createClient("client-1")
        val client2 = createClient("client-2")
        val client3 = createClient("client-3")

        client1.connect()
        assertLogContains("client-1", "Should log client-1 connection")
        println("✓ Client 1 connected")

        client2.connect()
        assertLogContains("client-2", "Should log client-2 connection")
        println("✓ Client 2 connected")

        client3.connect()
        assertLogContains("client-3", "Should log client-3 connection")
        println("✓ Client 3 connected")

        // Disconnect and verify
        client1.disconnect()
        assertLogContains("Disconnected: clientID=client-1")

        client2.disconnect()
        client3.disconnect()

        println("✓ TC-N3 PASSED: Multiple clients connected and disconnected successfully")
    }

    /**
     * TC-N4 & TC-N5: Test publish/subscribe loopback with broker logs.
     */
    @Test
    fun testPublishSubscribeLoopbackWithLogs() {
        println("\n=== TC-N4 & TC-N5: Pub/Sub Loopback with Logs ===")

        val config = createDefaultConfig()
        createBroker(config)

        val testTopic = "home/test"
        val testMessage = "Test Message"
        val messageReceived = CountDownLatch(1)
        var receivedMessage: String? = null

        // Create subscriber
        val subscriberClient = createAsyncClient("subscriber")
        subscriberClient.connect().get(10, TimeUnit.SECONDS)

        // Subscribe to topic
        subscriberClient.subscribeWith()
            .topicFilter(testTopic)
            .callback { publish ->
                val payload = publish.payload.orElse(null)
                receivedMessage = if (payload != null) {
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

        // TC-N5: Verify subscription logged
        assertLogContains("Subscribed: topic=$testTopic")
        println("✓ TC-N5: Subscription logged in broker")

        // Create publisher
        val publisherClient = createClient("publisher")
        publisherClient.connect()

        // Publish message
        publisherClient.publishWith()
            .topic(testTopic)
            .payload(testMessage.toByteArray())
            .send()

        // TC-N5: Verify publish logged
        assertLogContains("Published: topic=$testTopic")
        println("✓ TC-N5: Publish logged in broker")

        // Wait for message
        val received = messageReceived.await(testTimeoutMs, TimeUnit.MILLISECONDS)
        assertTrue(received, "Timeout waiting for message")
        assertTrue(receivedMessage == testMessage, "Message mismatch")

        println("✓ TC-N4 PASSED: Publish/subscribe loopback successful")
        println("✓ TC-N5 PASSED: Broker logs captured connection, subscription, and publish events")

        publisherClient.disconnect()
        subscriberClient.disconnect().get(5, TimeUnit.SECONDS)
    }

    /**
     * TC-N6: Test broker stability with multiple clients.
     */
    @Test
    fun testMultipleClientsStability() {
        println("\n=== TC-N6: Multiple Clients Stability ===")

        val config = createDefaultConfig()
        createBroker(config)

        val testTopic = "home/multi"
        val subscriber1Messages = mutableListOf<String>()
        val subscriber2Messages = mutableListOf<String>()
        val message1Latch = CountDownLatch(2) // Both clients should receive
        val message2Latch = CountDownLatch(2)

        // Create two subscriber clients
        val subscriber1 = createAsyncClient("subscriber-1")
        val subscriber2 = createAsyncClient("subscriber-2")

        subscriber1.connect().get(10, TimeUnit.SECONDS)
        subscriber2.connect().get(10, TimeUnit.SECONDS)

        // Both subscribe to same topic
        subscriber1.subscribeWith()
            .topicFilter(testTopic)
            .callback { publish ->
                val payload = publish.payload.orElse(null)
                if (payload != null) {
                    val bytes = ByteArray(payload.remaining())
                    payload.duplicate().get(bytes)
                    subscriber1Messages.add(String(bytes))
                    message1Latch.countDown()
                    if (subscriber1Messages.size > 1) message2Latch.countDown()
                }
            }
            .send()
            .get(5, TimeUnit.SECONDS)

        subscriber2.subscribeWith()
            .topicFilter(testTopic)
            .callback { publish ->
                val payload = publish.payload.orElse(null)
                if (payload != null) {
                    val bytes = ByteArray(payload.remaining())
                    payload.duplicate().get(bytes)
                    subscriber2Messages.add(String(bytes))
                    message1Latch.countDown()
                    if (subscriber2Messages.size > 1) message2Latch.countDown()
                }
            }
            .send()
            .get(5, TimeUnit.SECONDS)

        // Verify both clients logged
        assertLogContains("subscriber-1", "Client 1 should be logged")
        assertLogContains("subscriber-2", "Client 2 should be logged")
        println("✓ Both clients connected and subscribed")

        // Publish from subscriber-1
        subscriber1.publishWith()
            .topic(testTopic)
            .payload("Message from client 1".toByteArray())
            .send()
            .get(5, TimeUnit.SECONDS)

        assertTrue(message1Latch.await(10, TimeUnit.SECONDS), "Both should receive message 1")
        println("✓ Both clients received message from client 1")

        // Publish from subscriber-2
        subscriber2.publishWith()
            .topic(testTopic)
            .payload("Message from client 2".toByteArray())
            .send()
            .get(5, TimeUnit.SECONDS)

        assertTrue(message2Latch.await(10, TimeUnit.SECONDS), "Both should receive message 2")
        println("✓ Both clients received message from client 2")

        // Verify no crashes
        assertTrue(subscriber1.state.isConnected, "Client 1 should still be connected")
        assertTrue(subscriber2.state.isConnected, "Client 2 should still be connected")

        println("✓ TC-N6 PASSED: Broker stable with multiple clients, all messages delivered")

        subscriber1.disconnect().get(5, TimeUnit.SECONDS)
        subscriber2.disconnect().get(5, TimeUnit.SECONDS)
    }

    /**
     * TC-N7: Test broker restart while clients connected.
     */
    @Test
    fun testBrokerRestartWithClients() {
        println("\n=== TC-N7: Broker Restart with Clients ===")

        val config = createDefaultConfig()
        createBroker(config)

        // Connect a client
        val client = createClient("restart-test-client")
        client.connect()
        assertTrue(client.state.isConnected, "Client should be connected")
        println("✓ Client connected to broker")

        // Stop broker
        println("Stopping broker...")
        mqttBroker?.stopServer()
        Thread.sleep(1000)

        // Client should be disconnected
        try {
            client.publishWith()
                .topic("test")
                .payload("test".toByteArray())
                .send()
            fail("Should not be able to publish to stopped broker")
        } catch (e: Exception) {
            println("✓ Client disconnected after broker stop")
        }

        // Restart broker
        println("Restarting broker...")
        testInterceptHandler.clear()
        createBroker(config)

        // Reconnect client
        val newClient = createClient("restart-test-client")
        newClient.connect()
        assertTrue(newClient.state.isConnected, "Client should reconnect")
        assertLogContains("restart-test-client", "Reconnection should be logged")

        println("✓ TC-N7 PASSED: Client reconnected after broker restart")

        newClient.disconnect()
    }

    /**
     * TC-N9: Test authentication with wrong credentials.
     */
    @Test
    fun testAuthenticationFailure() {
        println("\n=== TC-N9: Auth with Wrong Credentials ===")

        val correctUsername = "narada"
        val correctPassword = "narada123"

        // Start broker with auth enabled
        val config = createAuthConfig(correctUsername, correctPassword)
        createBroker(config)

        // Try to connect with wrong credentials
        try {
            val client = MqttClient.builder()
                .useMqttVersion3()
                .identifier("auth-test-client")
                .serverHost(brokerHost)
                .serverPort(brokerPort)
                .buildBlocking()

            client.connectWith()
                .simpleAuth()
                .username("wrong_user")
                .password("wrong_password".toByteArray())
                .applySimpleAuth()
                .send()

            fail("Should not be able to connect with wrong credentials")
        } catch (e: Exception) {
            println("✓ Connection rejected with wrong credentials: ${e.message}")
        }

        // Try with correct credentials
        val authClient = MqttClient.builder()
            .useMqttVersion3()
            .identifier("auth-success-client")
            .serverHost(brokerHost)
            .serverPort(brokerPort)
            .buildBlocking()

        authClient.connectWith()
            .simpleAuth()
            .username(correctUsername)
            .password(correctPassword.toByteArray())
            .applySimpleAuth()
            .send()

        assertTrue(authClient.state.isConnected, "Should connect with correct credentials")
        println("✓ Connection successful with correct credentials")

        println("✓ TC-N9 PASSED: Authentication working correctly")

        authClient.disconnect()
    }

    /**
     * TC-N10: Test WebSocket connection mode.
     */
    @Test
    fun testWebSocketConnection() {
        println("\n=== TC-N10: WebSocket Mode ===")

        val wsPort = 8080
        val wsPath = "/mqtt"

        // Start broker with WebSocket enabled
        val config = createWebSocketConfig(wsPort, wsPath)
        createBroker(config)

        // Connect via WebSocket
        val wsClient = MqttClient.builder()
            .useMqttVersion3()
            .identifier("websocket-client")
            .serverHost(brokerHost)
            .serverPort(wsPort)
            .webSocketConfig()
                .serverPath(wsPath)
                .applyWebSocketConfig()
            .buildBlocking()

        wsClient.connect()
        assertTrue(wsClient.state.isConnected, "WebSocket client should connect")
        assertLogContains("websocket-client", "WebSocket connection should be logged")
        println("✓ WebSocket client connected")

        // Test pub/sub over WebSocket
        val testTopic = "ws/test"
        val testMessage = "WebSocket Test Message"
        val messageReceived = CountDownLatch(1)
        var receivedMessage: String? = null

        val wsSubscriber = MqttClient.builder()
            .useMqttVersion3()
            .identifier("ws-subscriber")
            .serverHost(brokerHost)
            .serverPort(wsPort)
            .webSocketConfig()
                .serverPath(wsPath)
                .applyWebSocketConfig()
            .buildAsync()

        wsSubscriber.connect().get(10, TimeUnit.SECONDS)

        wsSubscriber.subscribeWith()
            .topicFilter(testTopic)
            .callback { publish ->
                val payload = publish.payload.orElse(null)
                receivedMessage = if (payload != null) {
                    val bytes = ByteArray(payload.remaining())
                    payload.duplicate().get(bytes)
                    String(bytes)
                } else {
                    ""
                }
                messageReceived.countDown()
            }
            .send()
            .get(5, TimeUnit.SECONDS)

        wsClient.publishWith()
            .topic(testTopic)
            .payload(testMessage.toByteArray())
            .send()

        assertTrue(messageReceived.await(5, TimeUnit.SECONDS), "Should receive message via WebSocket")
        assertTrue(receivedMessage == testMessage, "Message should match")

        println("✓ TC-N10 PASSED: WebSocket mode working, pub/sub successful")

        wsClient.disconnect()
        wsSubscriber.disconnect().get(5, TimeUnit.SECONDS)
    }

    // Helper methods

    private fun createClient(identifier: String): Mqtt3BlockingClient {
        return MqttClient.builder()
            .useMqttVersion3()
            .identifier(identifier)
            .serverHost(brokerHost)
            .serverPort(brokerPort)
            .buildBlocking()
    }

    private fun createAsyncClient(identifier: String) =
        MqttClient.builder()
            .useMqttVersion3()
            .identifier(identifier)
            .serverHost(brokerHost)
            .serverPort(brokerPort)
            .buildAsync()
}



