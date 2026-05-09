package `in`.eswarm.mahati.mqtt.core

import `in`.eswarm.mahati.db.AppMqttMessage
import `in`.eswarm.mahati.db.MqttConnectionModel
import `in`.eswarm.mahati.mqtt.common.MqttClientState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining the contract for MQTT operations.
 * This provides an abstraction over the specific MQTT client implementation.
 */
interface MqttManager {

    /**
     * A flow representing the current state of the MQTT client connection.
     * UI layers can collect this flow to react to connection changes.
     */
    val connectionState: StateFlow<MqttClientState>

    /**
     * A shared flow that emits messages received from subscribed topics.
     * Multiple collectors can listen to incoming messages.
     */
    val receivedMessages: SharedFlow<AppMqttMessage>

    /**
     * Optional callback invoked on each successful connect/reconnect.
     * The controller uses this to restore persisted subscriptions.
     */
    var onReconnected: (suspend () -> Unit)?

    /**
     * Attempts to connect to the MQTT broker with the given parameters.
     * Connection status updates will be emitted via [connectionState].
     * This method should handle reconnections internally if desired by the implementation.
     *
     * @param params The connection parameters.
     * @param autoReconnect Whether to automatically reconnect on connection loss.
     */
    fun connect(params: MqttConnectionModel, autoReconnect: Boolean)

    /**
     * Disconnects from the MQTT broker.
     * Connection status updates will be emitted via [connectionState].
     */
    fun disconnect()

    /**
     * Publishes a message to a given topic.
     *
     * @param topic The topic to publish to.
     * @param message The message payload as a String.
     * @param qos The Quality of Service level (0, 1, or 2).
     * @param retain Whether the message should be retained by the broker.
     * @return True if publishing was initiated successfully, false otherwise (e.g., not connected).
     *         Note: Successful initiation doesn't guarantee delivery for QoS 0.
     */
    suspend fun publish(topic: String, message: String, qos: Int = 1, retain: Boolean = true): Boolean

    /**
     * Publishes a message to a given topic.
     *
     * @param topic The topic to publish to.
     * @param payload The message payload as a ByteArray.
     * @param qos The Quality of Service level (0, 1, or 2).
     * @param retain Whether the message should be retained by the broker.
     * @return True if publishing was initiated successfully, false otherwise (e.g., not connected).
     */
    suspend fun publish(topic: String, payload: ByteArray, qos: Int = 1, retain: Boolean = true): Boolean

    /**
     * Subscribes to a given topic filter.
     * Received messages will be emitted via [receivedMessages].
     *
     * @param topicFilter The topic filter to subscribe to (e.g., "my/topic", "my/+/topic").
     * @param qos The maximum Quality of Service level for messages on this subscription.
     * @return True if subscription was initiated successfully, false otherwise (e.g., not connected).
     */
    suspend fun subscribe(topicFilter: String, qos: Int = 1): Boolean

    /**
     * Unsubscribes from a given topic filter.
     *
     * @param topicFilter The topic filter to unsubscribe from.
     * @return True if unsubscription was initiated successfully, false otherwise (e.g., not connected).
     */
    suspend fun unsubscribe(topicFilter: String): Boolean

    /**
     * Cleans up resources used by the MqttManager.
     * Should be called when the manager is no longer needed.
     */
    fun cleanup()
}
