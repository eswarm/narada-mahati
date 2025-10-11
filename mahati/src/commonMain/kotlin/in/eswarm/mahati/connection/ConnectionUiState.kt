package `in`.eswarm.mahati.connection

data class ConnectionUiState(
    val clientID: String = "",
    val clientIDError: String? = null,
    val hostname: String = "",
    val hostnameError: String? = null,
    val port: String = "", // Keep as String for input, convert to Int in ViewModel
    val portError: String? = null,
    val username: String = "",
    val password: String = "",
    val useSsl: Boolean = false,
    val useWebsockets: Boolean = false,
    val isConnecting: Boolean = false, // True if any connection attempt is in progress
    val connectingClientId: String? = null, // The clientID of the connection attempt
    val connectionError: String? = null,
    val connectionSuccess: Boolean = false
)
