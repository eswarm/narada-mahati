package `in`.eswarm.mahati.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import `in`.eswarm.mahati.connection.ConnectionViewModel
import `in`.eswarm.mahati.db.ConnectionRepository
import `in`.eswarm.mahati.db.MqttConnectionParamsEntity
import `in`.eswarm.mahati.mqtt.common.MqttConnectionParams
import `in`.eswarm.mahati.mqtt.core.MqttManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.reflect.KClass

sealed interface HomeUiEvent {
    data object AddNewConnectionClicked : HomeUiEvent
    data class ConnectionSelected(val profileId: String) : HomeUiEvent
}

// Define states or one-time actions the ViewModel can send to the UI (for navigation etc.)
sealed interface HomeSideEffect {
    data object NavigateToNewConnectionScreen : HomeSideEffect
    data class NavigateToConnectionDetails(val profileId: String) : HomeSideEffect
}

class HomeViewModel(val connectionRepo: ConnectionRepository) : ViewModel() {
    var profiles: Flow<List<MqttConnectionProfile>> = MutableStateFlow(emptyList())

    private fun toProfile(connection: List<MqttConnectionParamsEntity>): List<MqttConnectionProfile> {
        return connection.map { entity ->
            MqttConnectionProfile(
                UUID.randomUUID().toString(), entity.brokerHost, MqttConnectionParams(
                    entity.brokerHost, entity.brokerPort.toInt(), entity.clientId
                )
            )
        }
    }

    private val _sideEffects = MutableStateFlow<HomeSideEffect?>(null)
    val sideEffects: StateFlow<HomeSideEffect?> = _sideEffects.asStateFlow()

    init {
        loadConnectionProfiles()
    }

    private fun loadConnectionProfiles() {
        viewModelScope.launch {
            profiles =
                connectionRepo.getAllConnectionsFlow().map { connection -> toProfile(connection) }
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

    companion object {
        fun Factory(
            connectionRepo: ConnectionRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")

            override fun <T : ViewModel> create(
                modelClass: KClass<T>, extras: CreationExtras
            ): T {
                if (modelClass.java.isAssignableFrom(HomeViewModel::class.java)) {
                    return HomeViewModel(connectionRepo) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

}
