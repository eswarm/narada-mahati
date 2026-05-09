package `in`.eswarm.mahati.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import `in`.eswarm.mahati.data.data.SettingsDataStore
import kotlinx.coroutines.launch

class SettingsViewModel(val settingsDataStore: SettingsDataStore) : ViewModel() {

    fun setAutoReconnect(value: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setAutoReconnect(value)
        }
    }

    fun setWakeLock(value: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setWakeLock(value)
        }
    }

    class Factory(private val settingsDataStore: SettingsDataStore) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: kotlin.reflect.KClass<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsDataStore) as T
        }
    }
}
