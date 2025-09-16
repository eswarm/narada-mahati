package `in`.eswarm.narada.mqtt

data class ServerProperties(
    val mqttPort: Int,
    val wsEnabled: Boolean,
    val wsPort: Int,
    val wsPath: String,
    val authEnabled: Boolean,
    val userName: String,
    val password: String
)