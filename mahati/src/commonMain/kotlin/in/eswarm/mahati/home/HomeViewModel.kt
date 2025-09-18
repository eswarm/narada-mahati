package `in`.eswarm.mahati.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import `in`.eswarm.mahati.db.ConnectionRepository
import `in`.eswarm.mahati.db.MqttConnectionParamsEntity
import `in`.eswarm.mahati.mqtt.core.MqttManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

sealed interface HomeUiEvent {
    data object AddNewConnectionClicked : HomeUiEvent
    data class ConnectionSelected(val clientID: String) : HomeUiEvent
}

// Define states or one-time actions the ViewModel can send to the UI (for navigation etc.)
sealed interface HomeSideEffect {
    data object NavigateToNewConnectionScreen : HomeSideEffect
    data class NavigateToConnectionDetails(val clientID: String) : HomeSideEffect
}

class HomeViewModel(val connectionRepo: ConnectionRepository, val mqttManager: MqttManager) :
    ViewModel() {
    var profiles: Flow<List<MqttConnectionParamsEntity>> = MutableStateFlow(emptyList())

    private val _sideEffects = MutableStateFlow<HomeSideEffect?>(null)
    val sideEffects: StateFlow<HomeSideEffect?> = _sideEffects.asStateFlow()

    init {
        loadConnectionProfiles()
    }

    private fun loadConnectionProfiles() {
        viewModelScope.launch {
            profiles =
                connectionRepo.getAllConnectionsFlow()
        }
    }

    fun onEvent(event: HomeUiEvent) {
        viewModelScope.launch {
            when (event) {
                is HomeUiEvent.AddNewConnectionClicked -> {
                    _sideEffects.value = HomeSideEffect.NavigateToNewConnectionScreen
                }

                is HomeUiEvent.ConnectionSelected -> {
                    val params = connectionRepo.getConnectionByClientId(event.clientID)
                    if (params != null) {
                        mqttManager.connect(params)
                    }
                }
            }
        }
    }

    fun clearSideEffect() {
        _sideEffects.value = null
    }

    companion object {
        fun Factory(
            connectionRepo: ConnectionRepository,
            mqttManager: MqttManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")

            override fun <T : ViewModel> create(
                modelClass: KClass<T>, extras: CreationExtras
            ): T {
                if (modelClass.java.isAssignableFrom(HomeViewModel::class.java)) {
                    return HomeViewModel(connectionRepo, mqttManager) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

}
