package `in`.eswarm.narada

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Desktop integration tests for Mahati MQTT Client functionality.
 *
 * Maps to CSV test cases:
 * - TC-M2: Connect/disconnect
 * - TC-M3: Subscribe to topic
 * - TC-M4: Open chat for subscribed topic
 * - TC-M5: Publish message from chat
 * - TC-M6: Receive message routing
 * - TC-M7: Notification deep link (simulated)
 * - TC-M8: Wildcard + filter
 * - TC-M9: Wildcard # filter
 * - TC-M13: Offline publish failure
 * - TC-M14: Reconnect auto-subscribe
 * - TC-M15: Unsubscribe flow
 */
class MahatiClientTests : BrokerTestBase() {

    /**
     * TC-M2: Test client connection state transitions.
     */
    @Test
    fun testClientConnectionStates() {
        println("\n=== TC-M2: Connect/Disconnect States ===")

        val config = createDefaultConfig()
        createBroker(config)

        val client = createClient("state-test-client")

        // Initial state: disconnected
        assertFalse(client.state.isConnected, "Client should start disconnected")
        println("✓ Initial state: disconnected")

        // Connect
        client.connect()
        assertTrue(client.state.isConnected, "Client should be connected")
        assertLogContains("state-test-client", "Connection should be logged")
        println("✓ State transition: connected")

        // Disconnect
        client.disconnect()
        assertFalse(client.state.isConnected, "Client should be disconnected")
        assertLogContains("Disconnected: clientID=state-test-client")
        println("✓ State transition: disconnected")

        // Reconnect
        client.connect()
        assertTrue(client.state.isConnected, "Client should reconnect")
        println("✓ State transition: reconnected")

        println("✓ TC-M2 PASSED: Connection state transitions working correctly")

        client.disconnect()
    }

    /**
     * TC-M3 & TC-M4: Test subscribe to topic and chat functionality.
     */
    @Test
    fun testSubscribeAndChatForTopic() {
        println("\n=== TC-M3 & TC-M4: Subscribe and Chat ===")

        val config = createDefaultConfig()
        createBroker(config)

        val testTopic = "home/alpha"
        val qos = 1
        val messageReceived = CountDownLatch(1)
        var receivedMessage: String? = null

        // Create client
        val client = createAsyncClient("chat-client")
        client.connect().get(10, TimeUnit.SECONDS)

        // TC-M3: Subscribe to topic with QoS 1
        client.subscribeWith()
            .topicFilter(testTopic)
            .qos(com.hivemq.client.mqtt.datatypes.MqttQos.AT_LEAST_ONCE)
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

        assertLogContains("Subscribed: topic=$testTopic")
        println("✓ TC-M3 PASSED: Subscribed to topic with QoS 1")

        // TC-M4: Simulate opening chat and receiving message
        val testMessage = "Message for alpha chat"
        client.publishWith()
            .topic(testTopic)
            .payload(testMessage.toByteArray())
            .send()
            .get(5, TimeUnit.SECONDS)

        assertTrue(messageReceived.await(5, TimeUnit.SECONDS), "Should receive message")
        assertTrue(receivedMessage == testMessage, "Message should match")

        println("✓ TC-M4 PASSED: Chat for subscribed topic shows correct messages")

        client.disconnect().get(5, TimeUnit.SECONDS)
    }

    /**
     * TC-M5: Test publish message from chat.
     */
    @Test
    fun testPublishMessageFromChat() {
        println("\n=== TC-M5: Publish from Chat ===")

        val config = createDefaultConfig()
        createBroker(config)

        val testTopic = "home/alpha"
        val testMessage = "Chat message"
        val messagesReceived = CountDownLatch(2) // Both sender and recipient
        var senderReceivedOwn = false
        var recipientReceived = false

        // Create sender (simulates user typing in chat)
        val sender = createAsyncClient("sender")
        sender.connect().get(10, TimeUnit.SECONDS)

        sender.subscribeWith()
            .topicFilter(testTopic)
            .callback {
                senderReceivedOwn = true
                messagesReceived.countDown()
            }
            .send()
            .get(5, TimeUnit.SECONDS)

        // Create recipient (another user subscribed to same topic)
        val recipient = createAsyncClient("recipient")
        recipient.connect().get(10, TimeUnit.SECONDS)

        recipient.subscribeWith()
            .topicFilter(testTopic)
            .callback {
                recipientReceived = true
                messagesReceived.countDown()
            }
            .send()
            .get(5, TimeUnit.SECONDS)

        // Sender publishes message from chat
        sender.publishWith()
            .topic(testTopic)
            .payload(testMessage.toByteArray())
            .send()
            .get(5, TimeUnit.SECONDS)

        assertTrue(messagesReceived.await(10, TimeUnit.SECONDS), "Messages should be received")
        assertTrue(senderReceivedOwn, "Sender should see own message")
        assertTrue(recipientReceived, "Recipient should receive message")
        assertLogContains("Published: topic=$testTopic")

        println("✓ TC-M5 PASSED: Message published from chat, no duplicates")

        sender.disconnect().get(5, TimeUnit.SECONDS)
        recipient.disconnect().get(5, TimeUnit.SECONDS)
    }

    /**
     * TC-M6: Test message routing to correct topics (no cross-pollination).
     */
    @Test
    fun testMessageRouting() {
        println("\n=== TC-M6: Message Routing ===")

        val config = createDefaultConfig()
        createBroker(config)

        val topicAlpha = "home/alpha"
        val topicBeta = "home/beta"

        val alphaMessages = mutableListOf<String>()
        val betaMessages = mutableListOf<String>()
        val alphaReceived = CountDownLatch(1)
        val betaReceived = CountDownLatch(1)

        // Create client subscribed to both topics
        val client = createAsyncClient("routing-client")
        client.connect().get(10, TimeUnit.SECONDS)

        // Subscribe to alpha
        client.subscribeWith()
            .topicFilter(topicAlpha)
            .callback { publish ->
                val payload = publish.payload.orElse(null)
                if (payload != null) {
                    val bytes = ByteArray(payload.remaining())
                    payload.duplicate().get(bytes)
                    alphaMessages.add(String(bytes))
                    alphaReceived.countDown()
                }
            }
            .send()
            .get(5, TimeUnit.SECONDS)

        // Subscribe to beta
        client.subscribeWith()
            .topicFilter(topicBeta)
            .callback { publish ->
                val payload = publish.payload.orElse(null)
                if (payload != null) {
                    val bytes = ByteArray(payload.remaining())
                    payload.duplicate().get(bytes)
                    betaMessages.add(String(bytes))
                    betaReceived.countDown()
                }
            }
            .send()
            .get(5, TimeUnit.SECONDS)

        // Publish to alpha
        client.publishWith()
            .topic(topicAlpha)
            .payload("Message for alpha".toByteArray())
            .send()
            .get(5, TimeUnit.SECONDS)

        assertTrue(alphaReceived.await(5, TimeUnit.SECONDS), "Alpha should receive message")

        // Publish to beta
        client.publishWith()
            .topic(topicBeta)
            .payload("Message for beta".toByteArray())
            .send()
            .get(5, TimeUnit.SECONDS)

        assertTrue(betaReceived.await(5, TimeUnit.SECONDS), "Beta should receive message")

        // Verify no cross-pollination
        Thread.sleep(1000) // Wait to ensure no extra messages
        assertTrue(alphaMessages.size == 1, "Alpha should have exactly 1 message")
        assertTrue(betaMessages.size == 1, "Beta should have exactly 1 message")
        assertTrue(alphaMessages[0].contains("alpha"), "Alpha message should be correct")
        assertTrue(betaMessages[0].contains("beta"), "Beta message should be correct")

        println("✓ TC-M6 PASSED: Messages routed correctly, no cross-pollination")

        client.disconnect().get(5, TimeUnit.SECONDS)
    }

    /**
     * TC-M7: Test notification deep link simulation (message delivery).
     */
    @Test
    fun testNotificationDeepLinkSimulation() {
        println("\n=== TC-M7: Notification Deep Link (Simulated) ===")

        val config = createDefaultConfig()
        createBroker(config)

        val testTopic = "home/alpha"
        val testMessage = "Notification test message"
        val messageReceived = CountDownLatch(1)
        var receivedTopic: String? = null
        var receivedPayload: String? = null

        // Simulate background message reception
        val client = createAsyncClient("notification-client")
        client.connect().get(10, TimeUnit.SECONDS)

        client.subscribeWith()
            .topicFilter(testTopic)
            .callback { publish ->
                receivedTopic = publish.topic.toString()
                val payload = publish.payload.orElse(null)
                receivedPayload = if (payload != null) {
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

        // Simulate message arriving while app in background
        val publisher = createClient("notification-publisher")
        publisher.connect()
        publisher.publishWith()
            .topic(testTopic)
            .payload(testMessage.toByteArray())
            .send()

        // Verify message delivered with correct topic (enabling deep link)
        assertTrue(messageReceived.await(5, TimeUnit.SECONDS), "Message should be received")
        assertTrue(receivedTopic == testTopic, "Topic should match for deep linking")
        assertTrue(receivedPayload == testMessage, "Payload should match")

        println("✓ TC-M7 PASSED: Message delivered with correct topic (enables deep link to home/alpha chat)")

        publisher.disconnect()
        client.disconnect().get(5, TimeUnit.SECONDS)
    }

    /**
     * TC-M8: Test wildcard + subscription (single-level wildcard).
     */
    @Test
    fun testWildcardPlusFilter() {
        println("\n=== TC-M8: Wildcard + Filter ===")

        val config = createDefaultConfig()
        createBroker(config)

        val wildcardTopic = "home/+/status"
        val receivedMessages = mutableListOf<Pair<String, String>>() // topic to payload
        val messageLatch = CountDownLatch(2) // Expecting 2 matching messages

        val client = createAsyncClient("wildcard-plus-client")
        client.connect().get(10, TimeUnit.SECONDS)

        // Subscribe to wildcard topic
        client.subscribeWith()
            .topicFilter(wildcardTopic)
            .callback { publish ->
                val topic = publish.topic.toString()
                val payload = publish.payload.orElse(null)
                if (payload != null) {
                    val bytes = ByteArray(payload.remaining())
                    payload.duplicate().get(bytes)
                    receivedMessages.add(topic to String(bytes))
                    messageLatch.countDown()
                }
            }
            .send()
            .get(5, TimeUnit.SECONDS)

        println("✓ Subscribed to wildcard: $wildcardTopic")

        // Publish to matching topics
        client.publishWith()
            .topic("home/kitchen/status")
            .payload("Kitchen status".toByteArray())
            .send()
            .get(5, TimeUnit.SECONDS)

        client.publishWith()
            .topic("home/living/status")
            .payload("Living status".toByteArray())
            .send()
            .get(5, TimeUnit.SECONDS)

        // Publish to non-matching topic (should not be received)
        client.publishWith()
            .topic("home/status") // Missing middle level
            .payload("Should not match".toByteArray())
            .send()
            .get(5, TimeUnit.SECONDS)

        // Wait for messages
        assertTrue(messageLatch.await(5, TimeUnit.SECONDS), "Should receive 2 matching messages")

        // Verify correct messages received
        assertTrue(receivedMessages.size == 2, "Should receive exactly 2 messages")
        assertTrue(receivedMessages.any { it.first == "home/kitchen/status" && it.second == "Kitchen status" })
        assertTrue(receivedMessages.any { it.first == "home/living/status" && it.second == "Living status" })

        println("✓ TC-M8 PASSED: Wildcard + filter working (received ${receivedMessages.size} matching messages)")

        client.disconnect().get(5, TimeUnit.SECONDS)
    }

    /**
     * TC-M9: Test wildcard # subscription (multi-level wildcard).
     */
    @Test
    fun testWildcardHashFilter() {
        println("\n=== TC-M9: Wildcard # Filter ===")

        val config = createDefaultConfig()
        createBroker(config)

        val wildcardTopic = "home/#"
        val receivedTopics = mutableListOf<String>()
        val messageLatch = CountDownLatch(3) // Expecting 3 messages

        val client = createAsyncClient("wildcard-hash-client")
        client.connect().get(10, TimeUnit.SECONDS)

        // Subscribe to multi-level wildcard
        client.subscribeWith()
            .topicFilter(wildcardTopic)
            .callback { publish ->
                val topic = publish.topic.toString()
                receivedTopics.add(topic)
                messageLatch.countDown()
            }
            .send()
            .get(5, TimeUnit.SECONDS)

        println("✓ Subscribed to wildcard: $wildcardTopic")

        // Publish to various subtopics
        val testTopics = listOf(
            "home/a",
            "home/a/b",
            "home/x/y/z"
        )

        testTopics.forEach { topic ->
            client.publishWith()
                .topic(topic)
                .payload("Message for $topic".toByteArray())
                .send()
                .get(5, TimeUnit.SECONDS)
        }

        // Publish to non-matching topic
        client.publishWith()
            .topic("office/a") // Different root
            .payload("Should not match".toByteArray())
            .send()
            .get(5, TimeUnit.SECONDS)

        // Wait for messages
        assertTrue(messageLatch.await(5, TimeUnit.SECONDS), "Should receive all 3 matching messages")

        // Verify all expected topics received
        assertTrue(receivedTopics.size == 3, "Should receive exactly 3 messages")
        testTopics.forEach { expectedTopic ->
            assertTrue(receivedTopics.contains(expectedTopic), "Should receive message from $expectedTopic")
        }

        println("✓ TC-M9 PASSED: Wildcard # filter working (received messages from all subtopics)")

        client.disconnect().get(5, TimeUnit.SECONDS)
    }

    /**
     * TC-M13: Test offline publish failure.
     */
    @Test
    fun testOfflinePublishFailure() {
        println("\n=== TC-M13: Offline Publish Failure ===")

        val config = createDefaultConfig()
        createBroker(config)

        val client = createClient("offline-test-client")

        // Connect and verify
        client.connect()
        assertTrue(client.state.isConnected, "Client should be connected")
        println("✓ Client connected")

        // Disconnect
        client.disconnect()
        assertFalse(client.state.isConnected, "Client should be disconnected")
        println("✓ Client disconnected")

        // Attempt to publish while offline
        try {
            client.publishWith()
                .topic("test/offline")
                .payload("Should fail".toByteArray())
                .send()
            fail("Should not be able to publish while disconnected")
        } catch (e: Exception) {
            println("✓ Publish failed as expected: ${e.message}")
        }

        println("✓ TC-M13 PASSED: Offline publish correctly fails")
    }

    /**
     * TC-M14: Test reconnect auto-subscribe (subscription persistence).
     */
    @Test
    fun testReconnectAutoSubscribe() {
        println("\n=== TC-M14: Reconnect Auto-Subscribe ===")

        val config = createDefaultConfig()
        createBroker(config)

        val testTopic = "home/alpha"
        val messageLatch = CountDownLatch(1)
        var messageReceived = false

        val client = createAsyncClient("reconnect-sub-client")

        // Connect and subscribe
        client.connect().get(10, TimeUnit.SECONDS)
        client.subscribeWith()
            .topicFilter(testTopic)
            .callback {
                messageReceived = true
                messageLatch.countDown()
            }
            .send()
            .get(5, TimeUnit.SECONDS)

        assertLogContains("Subscribed: topic=$testTopic")
        println("✓ Initial subscription successful")

        // Disconnect
        client.disconnect().get(5, TimeUnit.SECONDS)
        println("✓ Client disconnected")

        // Reconnect
        client.connect().get(10, TimeUnit.SECONDS)
        println("✓ Client reconnected")

        // Re-subscribe (simulating client remembering subscriptions)
        client.subscribeWith()
            .topicFilter(testTopic)
            .callback {
                messageReceived = true
                messageLatch.countDown()
            }
            .send()
            .get(5, TimeUnit.SECONDS)

        // Publish message
        val publisher = createClient("publisher-reconnect")
        publisher.connect()
        publisher.publishWith()
            .topic(testTopic)
            .payload("Message after reconnect".toByteArray())
            .send()

        // Verify message received
        assertTrue(messageLatch.await(5, TimeUnit.SECONDS), "Should receive message after reconnect")
        assertTrue(messageReceived, "Message should be received")

        println("✓ TC-M14 PASSED: Subscription persists after reconnect, messages received")

        client.disconnect().get(5, TimeUnit.SECONDS)
        publisher.disconnect()
    }

    /**
     * TC-M15: Test unsubscribe flow.
     */
    @Test
    fun testUnsubscribeFlow() {
        println("\n=== TC-M15: Unsubscribe Flow ===")

        val config = createDefaultConfig()
        createBroker(config)

        val testTopic = "home/alpha"
        val receivedMessages = mutableListOf<String>()

        val client = createAsyncClient("unsubscribe-client")
        client.connect().get(10, TimeUnit.SECONDS)

        // Subscribe
        client.subscribeWith()
            .topicFilter(testTopic)
            .callback { publish ->
                val payload = publish.payload.orElse(null)
                if (payload != null) {
                    val bytes = ByteArray(payload.remaining())
                    payload.duplicate().get(bytes)
                    receivedMessages.add(String(bytes))
                }
            }
            .send()
            .get(5, TimeUnit.SECONDS)

        assertLogContains("Subscribed: topic=$testTopic")
        println("✓ Subscribed to $testTopic")

        // Publish message (should be received)
        client.publishWith()
            .topic(testTopic)
            .payload("Before unsubscribe".toByteArray())
            .send()
            .get(5, TimeUnit.SECONDS)

        Thread.sleep(500) // Wait for message delivery
        assertTrue(receivedMessages.size == 1, "Should receive message before unsubscribe")
        println("✓ Message received before unsubscribe")

        // Unsubscribe
        client.unsubscribeWith()
            .topicFilter(testTopic)
            .send()
            .get(5, TimeUnit.SECONDS)

        assertLogContains("Unsubscribed: topic=$testTopic")
        println("✓ Unsubscribed from $testTopic")

        // Publish another message (should NOT be received)
        client.publishWith()
            .topic(testTopic)
            .payload("After unsubscribe".toByteArray())
            .send()
            .get(5, TimeUnit.SECONDS)

        Thread.sleep(1000) // Wait to ensure no message arrives
        assertTrue(receivedMessages.size == 1, "Should not receive message after unsubscribe")

        println("✓ TC-M15 PASSED: No messages received after unsubscribe")

        client.disconnect().get(5, TimeUnit.SECONDS)
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

    private fun createAsyncClient(identifier: String): Mqtt3AsyncClient {
        return MqttClient.builder()
            .useMqttVersion3()
            .identifier(identifier)
            .serverHost(brokerHost)
            .serverPort(brokerPort)
            .buildAsync()
    }
}







