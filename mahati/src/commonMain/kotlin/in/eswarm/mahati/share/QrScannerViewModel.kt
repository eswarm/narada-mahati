package `in`.eswarm.mahati.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import `in`.eswarm.mahati.db.ConnectionAdapter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

class QrScannerViewModel(private val connectionRepository: ConnectionAdapter) : ViewModel() {

    fun onQrCodeScanned(json: String) {
        viewModelScope.launch {
            try {
                val details = Json.decodeFromString<ConnectionDetails>(json)
                connectionRepository.addConnection(
                    brokerHost = details.host,
                    brokerPort = details.port.toLong(),
                    clientID = "Scanned-${details.host}", // Create a default name
                    username = details.username,
                    password = details.password.toByteArray(),
                    useSsl = false, // Assuming no SSL from QR
                    useWebsocket = false, // Assuming no WebSocket from QR
                    webSocketPath = "",
                    topicPrefix = ""
                )
            } catch (e: Exception) {
                // Handle JSON parsing errors or other exceptions

            }
        }
    }

    companion object {
        fun Factory(connectionRepository: ConnectionAdapter): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(
                    modelClass: KClass<T>,
                    extras: CreationExtras
                ): T {
                    return QrScannerViewModel(connectionRepository) as T
                }
            }
    }
}
