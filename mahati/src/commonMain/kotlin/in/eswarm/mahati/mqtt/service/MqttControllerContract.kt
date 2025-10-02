
package `in`.eswarm.mahati.mqtt.service

import `in`.eswarm.mahati.db.AppMqttMessage
import `in`.eswarm.mahati.db.MqttConnection
import `in`.eswarm.mahati.mqtt.common.MqttClientState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * The common contract for the MQTT controller that ViewModels and other common code
 * will interact with. This interface hides all platform-specific implementation details
 * like Android Services or Desktop application scopes.
 */
interface MqttControllerContract {
    /**
     * A map of connection states, keyed by the user-defined connectionId.
     */
    val connectionStatesMap: StateFlow<Map<String, MqttClientState>>

    /**
     * A single flow emitting all messages from all active connections.
     * The message is paired with the connectionId it came from.
     */
    val allMessages: SharedFlow<Pair<String, AppMqttMessage>>?

    /**
     * Initializes and starts a new MQTT connection based on the provided config.
     *
     * @param config The platform-agnostic configuration for the connection.
     */
    fun addConnection(config: MqttConnection)

    /**
     * Disconnects and removes a previously added MQTT connection.
     *
     * @param connectionId The unique ID of the connection to remove.
     */
    fun removeConnection(connectionId: String)

    /**
     * Publishes a message to a topic on a specific connection.
     */
    fun publish(
        connectionId: String,
        topic: String,
        payload: ByteArray,
        qos: Int,
        retain: Boolean
    )

    /**
     * Subscribes to a topic on a specific connection.
     */
    fun subscribe(connectionId: String, topicFilter: String, qos: Int)

    /**
     * Shuts down all connections and cleans up resources.
     * This is typically called by the platform's lifecycle owner.
     */
    fun shutdownAll()
}
