package `in`.eswarm.mahati.mqtt.common

import kotlinx.serialization.Serializable

/**
 * Data class to hold all necessary parameters for establishing an MQTT connection.
 */
@Serializable
data class MqttConnectionParams(
    val brokerHost: String,
    val brokerPort: Int = 1883,
    val clientId: String,
    val username: String? = null,
    val password: ByteArray? = null, // Use ByteArray for sensitive data
    val useSsl: Boolean = false,
    val topicPrefix: String = "", // Optional prefix for all topics
    val version: Int = 0
    // TODO: Add more SSL specific configurations if needed (e.g., custom trust managers)
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MqttConnectionParams

        if (brokerHost != other.brokerHost) return false
        if (brokerPort != other.brokerPort) return false
        if (clientId != other.clientId) return false
        if (username != other.username) return false
        if (password != null) {
            if (other.password == null) return false
            if (!password.contentEquals(other.password)) return false
        } else if (other.password != null) return false
        if (useSsl != other.useSsl) return false
        if (topicPrefix != other.topicPrefix) return false
        if (version != other.version) return false

        return true
    }

    override fun hashCode(): Int {
        var result = brokerHost.hashCode()
        result = 31 * result + brokerPort.hashCode()
        result = 31 * result + clientId.hashCode()
        result = 31 * result + (username?.hashCode() ?: 0)
        result = 31 * result + (password?.contentHashCode() ?: 0)
        result = 31 * result + useSsl.hashCode()
        result = 31 * result + topicPrefix.hashCode()
        result = 31 * result + version
        return result
    }
}

/**
 * Represents the various states of the MQTT client.
 */
sealed interface MqttClientState {
    data object Disconnected :
        MqttClientState // Client is disconnected or connection attempt was cancelled.

    data object Connecting : MqttClientState   // Client is attempting to connect.
    data class Connected(val serverUri: String) :
        MqttClientState // Client is successfully connected.

    data class Error(val message: String, val cause: Throwable? = null) :
        MqttClientState // An error occurred.
}

/**
 * Represents a message received from the MQTT broker.
 */
data class MqttMessage(
    val topic: String,
    val payload: ByteArray,
    val qos: Int,
    val retained: Boolean
) {
    // Convenient way to get payload as String, assuming UTF-8.
    // Handle potential exceptions if the payload isn't valid UTF-8.
    val payloadAsText: String by lazy {
        try {
            String(payload, Charsets.UTF_8)
        } catch (e: Exception) {
            // Log error or return a placeholder
            "Error decoding payload: ${e.message}"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MqttMessage

        if (topic != other.topic) return false
        if (!payload.contentEquals(other.payload)) return false
        if (qos != other.qos) return false
        if (retained != other.retained) return false

        return true
    }

    override fun hashCode(): Int {
        var result = topic.hashCode()
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + qos
        result = 31 * result + retained.hashCode()
        return result
    }
}
