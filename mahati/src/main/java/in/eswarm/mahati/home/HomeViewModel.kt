package `in`.eswarm.mahati.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.eswarm.mahati.mqtt.common.MqttConnectionParams // Ensure this import is correct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

// Define events that the UI can send to the ViewModel
sealed interface HomeUiEvent {
    data object AddNewConnectionClicked : HomeUiEvent
    data class ConnectionSelected(val profileId: String) : HomeUiEvent
}

// Define states or one-time actions the ViewModel can send to the UI (for navigation etc.)
sealed interface HomeSideEffect {
    data object NavigateToNewConnectionScreen : HomeSideEffect
    data class NavigateToConnectionDetails(val profileId: String) : HomeSideEffect
}

class HomeViewModel : ViewModel() {

    private val _profiles = MutableStateFlow<List<MqttConnectionProfile>>(emptyList())
    val profiles: StateFlow<List<MqttConnectionProfile>> = _profiles.asStateFlow()

    private val _sideEffects = MutableStateFlow<HomeSideEffect?>(null)
    val sideEffects: StateFlow<HomeSideEffect?> = _sideEffects.asStateFlow()

    init {
        loadConnectionProfiles()
    }

    private fun loadConnectionProfiles() {
        viewModelScope.launch {
            // In a real app, load this from a repository (DataStore, Room, etc.)
            // For now, using sample data.
            _profiles.value = listOf(
                MqttConnectionProfile(
                    id = UUID.randomUUID().toString(),
                    name = "Mahati Local Mosquitto",
                    params = MqttConnectionParams(brokerHost = "192.168.1.10", clientId = "mahatiClient1")
                ),
                MqttConnectionProfile(
                    id = UUID.randomUUID().toString(),
                    name = "Mahati HiveMQ Public",
                    params = MqttConnectionParams(brokerHost = "public.hivemq.com", clientId = "mahatiClient2")
                ),
                MqttConnectionProfile(
                    id = UUID.randomUUID().toString(),
                    name = "Mahati Test Broker",
                    params = MqttConnectionParams(brokerHost = "test.mosquitto.org", clientId = "mahatiClient3")
                )
            )
        }
    }

    fun onEvent(event: HomeUiEvent) {
        viewModelScope.launch {
            when (event) {
                is HomeUiEvent.AddNewConnectionClicked -> {
                    _sideEffects.value = HomeSideEffect.NavigateToNewConnectionScreen
                }
                is HomeUiEvent.ConnectionSelected -> {
                    _sideEffects.value = HomeSideEffect.NavigateToConnectionDetails(event.profileId)
                }
            }
        }
    }

    fun clearSideEffect() {
        _sideEffects.value = null
    }
}
