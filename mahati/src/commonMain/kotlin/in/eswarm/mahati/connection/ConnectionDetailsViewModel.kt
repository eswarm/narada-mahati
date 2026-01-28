package `in`.eswarm.mahati.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import `in`.eswarm.mahati.db.ConnectionAdapter
import `in`.eswarm.mahati.db.MqttConnectionModel
import `in`.eswarm.mahati.mqtt.common.MqttClientState
import `in`.eswarm.mahati.mqtt.service.MqttControllerContract
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.String
import kotlin.reflect.KClass

class ConnectionDetailsViewModel(
    private val mqttController: MqttControllerContract,
    private val repo: ConnectionAdapter,
    private val clientID: String?,
    val onSuccess: () -> Unit
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectionUiState())
    val uiState: StateFlow<ConnectionUiState> = _uiState.asStateFlow()

    init {
        if (clientID != null) {
            updateDetails(clientID)
        }

        viewModelScope.launch {
            collectConnectionState()
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

    fun updateDetails(clientID: String) {
        viewModelScope.launch {
            val connection = repo.getConnectionByClientId(clientID)
            if (connection != null) {
                val details = ConnectionUiState(
                    hostname = connection.brokerHost,
                    port = connection.brokerPort.toString(),
                    clientID = connection.clientID,
                    username = connection.username ?: "",
                    password = connection.password?.decodeToString() ?: "",
                    useSsl = connection.useSsl,
                    useWebsockets = connection.useSsl
                )
                _uiState.update { details }
            }
        }
    }

    suspend fun collectConnectionState() {
        mqttController.connectionStatesMap.collect { stateMap ->
            val state = stateMap[uiState.value.clientID] ?: MqttClientState.Disconnected
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
                            isConnecting = true, connectionSuccess = false, connectionError = null
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

                        viewModelScope.launch {
                            delay(750)
                            onSuccess()
                        }

                        currentUiState.copy(
                            isConnecting = false, connectionSuccess = true, connectionError = null
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

        if (mqttController.connectionStatesMap.value[clientID] is MqttClientState.Connected) {
            _uiState.update { it.copy(connectionError = "Already Connected, not reconnecting") }
        }
        // isConnecting will be updated by the mqttManager.connectionState collector
        // when MqttClientState.Connecting is emitted.

        val params = MqttConnectionModel(
            id = 0,
            brokerHost = currentState.hostname,
            brokerPort = portNumber!!.toLong(),
            clientID = currentState.clientID,
            username = currentState.username,
            password = currentState.password.toByteArray(),
            useSsl = currentState.useSsl,
            topicPrefix = "",
            createdAt = System.currentTimeMillis()
        )
        mqttController.addConnection(params)
    }

    companion object {
        fun Factory(
            mqttController: MqttControllerContract,
            connectionRepo: ConnectionAdapter,
            clientId: String?,
            onSuccess: () -> Unit
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")

            override fun <T : ViewModel> create(
                modelClass: KClass<T>, extras: CreationExtras
            ): T {
                if (modelClass.java.isAssignableFrom(ConnectionDetailsViewModel::class.java)) {
                    return ConnectionDetailsViewModel(
                        mqttController, connectionRepo, clientId, onSuccess
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
