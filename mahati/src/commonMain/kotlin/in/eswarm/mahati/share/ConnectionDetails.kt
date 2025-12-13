package `in`.eswarm.mahati.share

import kotlinx.serialization.Serializable

/**
 * A data class representing the MQTT server connection details.
 * This class is used to deserialize the JSON from the QR code.
 */
@Serializable
data class ConnectionDetails(
    val host: String,
    val port: Int,
    val wsEnabled: Boolean,
    val wsPort: Int,
    val wsPath: String,
    val authEnabled: Boolean,
    val username: String,
    val password: String
)
