package `in`.eswarm.mahati.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import `in`.eswarm.mahati.db.ConnectionAdapter import `in`.eswarm.mahati.db.MqttConnection
import `in`.eswarm.mahati.mqtt.common.MqttClientState
import `in`.eswarm.mahati.mqtt.core.MqttManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class ConnectionViewModel(
    private val mqttManager: MqttManager,
    private val repo: ConnectionAdapter
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectionUiState())
    val uiState: StateFlow<ConnectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            mqttManager.connectionState.collect { state ->
                _uiState.update { currentUiState ->
                    when (state) {
                        MqttClientState.Disconnected -> {
                            currentUiState.copy(
                                isConnecting = false,
                                connectionError = if (currentUiState.isConnecting || currentUiState.connectionSuccess) "Disconnected" else null,
                                connectionSuccess = false
                            )
                        }

                        MqttClientState.Connecting -> {
                            currentUiState.copy(
                                isConnecting = true,
                                connectionSuccess = false,
                                connectionError = null
                            )
                        }

                        is MqttClientState.Connected -> {
                            repo.addConnection(
                                uiState.value.hostname,
                                uiState.value.port.toLong(),
                                uiState.value.clientID,
                                uiState.value.username,
                                uiState.value.password.toByteArray(),
                                uiState.value.useSsl,
                                ""
                            )

                            currentUiState.copy(
                                isConnecting = false,
                                connectionSuccess = true,
                                connectionError = null
                            )
                        }

                        is MqttClientState.Error -> {
                            currentUiState.copy(
                                isConnecting = false,
                                connectionSuccess = false,
                                connectionError = state.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun onClientIdChange(clientId: String) {
        _uiState.update {
            it.copy(
                clientID = clientId,
                clientIDError = null,
                connectionError = null,
                connectionSuccess = false
            )
        }
    }

    fun onHostnameChange(hostname: String) {
        _uiState.update {
            it.copy(
                hostname = hostname,
                hostnameError = null,
                connectionError = null,
                connectionSuccess = false
            )
        }
    }

    fun onPortChange(port: String) {
        _uiState.update {
            it.copy(
                port = port, portError = null, connectionError = null, connectionSuccess = false
            )
        }
    }

    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onUseSslChange(enabled: Boolean) {
        _uiState.update { it.copy(useSsl = enabled) }
    }

    fun onUseWebsocketsChange(enabled: Boolean) {
        // This state is for the UI checkbox; MqttConnectionParams does not use it currently.
        _uiState.update { it.copy(useWebsockets = enabled) }
    }

    fun connect() {
        val currentState = _uiState.value
        var isValid = true

        // Clear previous validation errors and connection status messages
        _uiState.update {
            it.copy(
                clientIDError = null,
                hostnameError = null,
                portError = null,
                connectionError = null,
                connectionSuccess = false,
                isConnecting = false
            )
        }

        if (currentState.clientID.isBlank()) {
            _uiState.update { it.copy(clientIDError = "Client ID cannot be empty") }
            isValid = false
        }

        if (currentState.hostname.isBlank()) {
            _uiState.update { it.copy(hostnameError = "Hostname cannot be empty") }
            isValid = false
        }

        val portNumber = currentState.port.toIntOrNull()
        if (currentState.port.isBlank()) {
            _uiState.update { it.copy(portError = "Port cannot be empty") }
            isValid = false
        } else if (portNumber == null || portNumber !in 1..65535) {
            _uiState.update { it.copy(portError = "Invalid port number") }
            isValid = false
        }

        if (!isValid) return

        // isConnecting will be updated by the mqttManager.connectionState collector
        // when MqttClientState.Connecting is emitted.

        val params = MqttConnection(
            id = 0,
            brokerHost = currentState.hostname,
            brokerPort = portNumber!!.toLong(),
            clientID = currentState.clientID,
            username = currentState.username.takeIf { it.isNotBlank() },
            password = currentState.password.takeIf { it.isNotBlank() }?.encodeToByteArray(),
            useSsl = currentState.useSsl,
            topicPrefix = "",
            createdAt = System.currentTimeMillis()
        )
        mqttManager.connect(params)
    }

    fun cancel() {
        // Reset input fields and any error/status messages, keeping checkbox states
        _uiState.update { current ->
            ConnectionUiState(
                useSsl = current.useSsl, useWebsockets = current.useWebsockets
            )
        }

        // Request disconnection
        val currentMqttState = mqttManager.connectionState.value
        if (currentMqttState is MqttClientState.Connected || currentMqttState is MqttClientState.Connecting) {
            mqttManager.disconnect()
        }
        // The MqttClientState.Disconnected emission from connectionState will update uiState.
    }

    companion object {
        fun Factory(
            mqttManager: MqttManager,
            connectionRepo: ConnectionAdapter
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")

            override fun <T : ViewModel> create(
                modelClass: KClass<T>, extras: CreationExtras
            ): T {
                if (modelClass.java.isAssignableFrom(ConnectionViewModel::class.java)) {
                    return ConnectionViewModel(mqttManager, connectionRepo) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")

            }
        }
    }
}
