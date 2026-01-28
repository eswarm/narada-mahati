package `in`.eswarm.mahati.mqtt.service

import `in`.eswarm.mahati.db.AppMqttMessage
import `in`.eswarm.mahati.db.MqttConnection
import `in`.eswarm.mahati.db.MqttConnectionModel
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
    fun addConnection(config: MqttConnectionModel)

    /**
     * Disconnects and removes a previously added MQTT connection.
     *
     * @param clientID The unique ID of the connection to remove.
     */
    fun removeConnection(clientID: String)

    /**
     * Publishes a message to a topic on a specific connection.
     * This is a suspend function that returns true on success and false on failure.
     */
    suspend fun publish(
        clientID: String,
        topic: String,
        payload: ByteArray,
        qos: Int,
        retain: Boolean
    ): Boolean?

    /**
     * Subscribes to a topic on a specific connection.
     * This is a suspend function that returns true on success and false on failure.
     */
    suspend fun subscribe(clientID: String, topicFilter: String, qos: Int): Boolean?

    /**
     * Unsubscribes to a topic on a specific connection.
     * This is a suspend function that returns true on success and false on failure.
     */
    suspend fun unsubscribe(clientID: String, topicFilter: String): Boolean?

    /**
     * Shuts down all connections and cleans up resources.
     * This is typically called by the platform's lifecycle owner.
     */
    fun shutdownAll()
}
