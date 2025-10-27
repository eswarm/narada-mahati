package `in`.eswarm.mahati.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import `in`.eswarm.mahati.connection.ConnectionUiState
import `in`.eswarm.mahati.db.ConnectionAdapter
import `in`.eswarm.mahati.db.MqttConnection
import `in`.eswarm.mahati.home.HomeSideEffect.*
import `in`.eswarm.mahati.mqtt.common.MqttClientState
import `in`.eswarm.mahati.mqtt.service.MqttControllerContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

sealed interface HomeUiEvent {
    data object AddNewConnectionClicked : HomeUiEvent
    data class ConnectionSelected(val clientID: String) : HomeUiEvent
    data object SettingsClicked : HomeUiEvent
    data class EditConnectionClicked(val clientID: String) : HomeUiEvent
    data class DeleteConnectionClicked(val clientID: String) : HomeUiEvent
}

sealed interface HomeSideEffect {
    data object NavigateToNewConnectionScreen : HomeSideEffect
    data class NavigateToConnectionDetails(val clientID: String) : HomeSideEffect

    data object NavigateToSettingsScreen : HomeSideEffect

    data class DeleteConnection(val clientID: String) : HomeSideEffect

    data class EditConnection(val clientID: String) : HomeSideEffect
}

data class HomeUiState(
    val isConnecting: Boolean = false,
)


class HomeViewModel(
    private val connectionRepo: ConnectionAdapter,
    private val mqttController: MqttControllerContract
) : ViewModel() {

    var profiles: Flow<List<MqttConnection>> = MutableStateFlow(emptyList())
        private set

    private val _sideEffects = MutableStateFlow<HomeSideEffect?>(null)
    val sideEffects: StateFlow<HomeSideEffect?> = _sideEffects.asStateFlow()

    private val _uiState = MutableStateFlow(ConnectionUiState())
    val uiState: StateFlow<ConnectionUiState> = _uiState.asStateFlow()
    val mqttConnectionStates: StateFlow<Map<String, MqttClientState>> =
        mqttController.connectionStatesMap
    var clientID: String? = null

    init {
        loadConnectionProfiles()
        viewModelScope.launch {
            mqttConnectionStates.collect { map ->
                val state = map[clientID]
                if (state != null) {
                    onMqttStateUpdate(state)
                }
            }
        }
    }

    fun onMqttStateUpdate(newState: MqttClientState) {
        _uiState.update { currentUiState ->
            when (newState) {
                MqttClientState.Disconnected -> {
                    // Reset connection progress only if we were actively trying to connect this client
                    currentUiState.copy(
                        isConnecting = false,
                        connectingClientId = null,
                        connectionError = "Disconnected",
                        connectionSuccess = false // Always false on disconnect
                    )
                }

                MqttClientState.Connecting -> {
                    currentUiState.copy(
                        isConnecting = true, // Ensure this is true
                        connectionSuccess = false,
                        connectionError = null
                    )
                }

                is MqttClientState.Connected -> {
                    // Check if the connected client is the one we intended to connect for navigation
                    if (clientID == newState.clientID) {
                        clientID = null
                        _sideEffects.value =
                            HomeSideEffect.NavigateToConnectionDetails(newState.clientID)
                    }
                    currentUiState.copy(
                        isConnecting = false, // Connection attempt finished
                        connectingClientId = null, // Clear the specific client ID
                        connectionSuccess = true, connectionError = null
                    )
                }

                is MqttClientState.Error -> {
                    currentUiState.copy(
                        isConnecting = false,
                        connectingClientId = null,
                        connectionSuccess = false,
                        connectionError = newState.message
                    )
                }
            }
        }
    }

    private fun loadConnectionProfiles() {
        viewModelScope.launch {
            profiles = connectionRepo.getAllConnectionsFlow()
        }
    }

    fun onEvent(event: HomeUiEvent) {
        viewModelScope.launch {
            when (event) {
                is HomeUiEvent.AddNewConnectionClicked -> {
                    _sideEffects.value = HomeSideEffect.NavigateToNewConnectionScreen
                }

                is HomeUiEvent.ConnectionSelected -> {
                    // Enforce one connection attempt at a time
                    if (_uiState.value.isConnecting) {
                        // Optionally: show a message to the user that another connection is in progress
                        // For now, we simply ignore the request.
                        println("HomeViewModel: Connection attempt ignored, another is in progress for ${_uiState.value.connectingClientId}")
                        return@launch
                    }

                    val params = connectionRepo.getConnectionByClientId(event.clientID)
                    if (params != null) {
                        clientID = params.clientID // Set for navigation upon successful connection
                        val state = mqttController.connectionStatesMap.value[params.clientID]
                        if (state is MqttClientState.Connected) {
                            clientID = null
                            _sideEffects.value =
                                NavigateToConnectionDetails(params.clientID)
                        } else {
                            _uiState.update {
                                it.copy(
                                    isConnecting = true,
                                    connectingClientId = params.clientID,
                                    connectionError = null,
                                    connectionSuccess = false
                                )
                            }
                            mqttController.addConnection(params)
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isConnecting = false, // Ensure this is false if params are null
                                connectingClientId = null,
                                connectionError = "Connection profile not found for ${event.clientID}"
                            )
                        }
                    }
                }

                is HomeUiEvent.DeleteConnectionClicked -> {
                    deleteConnection(event.clientID)
                }

                is HomeUiEvent.EditConnectionClicked -> {
                    _sideEffects.value =
                        EditConnection(event.clientID)
                }

                HomeUiEvent.SettingsClicked -> {
                    _sideEffects.value =
                        NavigateToSettingsScreen
                }
            }
        }
    }

    fun clearSideEffect() {
        _sideEffects.value = null
    }

    fun deleteConnection(clientID: String) {
        viewModelScope.launch {
            connectionRepo.deleteConnectionByClientId(clientID)
            loadConnectionProfiles()
        }
    }

    fun clearConnectionError() {
        _uiState.value = _uiState.value.copy(connectionError = null)
    }
    companion object {
        fun Factory(
            connectionRepo: ConnectionAdapter, mqttController: MqttControllerContract
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                if (modelClass.java.isAssignableFrom(HomeViewModel::class.java)) {
                    return HomeViewModel(connectionRepo, mqttController) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}