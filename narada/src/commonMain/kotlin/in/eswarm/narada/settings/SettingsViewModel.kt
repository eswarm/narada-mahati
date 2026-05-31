package `in`.eswarm.narada.settings

import `in`.eswarm.narada.preferences.AppPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass
import kotlinx.coroutines.launch

class SettingsViewModel(val appPreferences: AppPreferences) : ViewModel() {

    fun setMqttPort(value: String): Boolean {
        val port: Int
        try {
            port = value.toInt()
            if (port < 1024) {
                return false
            }
        } catch (e: NumberFormatException) {
            return false
        }

        viewModelScope.launch {
            appPreferences.setMqttPort(port)
        }
        return true
    }

    fun setWSEnabled(value: Boolean) {
        viewModelScope.launch {
            appPreferences.setWSEnabled(value)
        }
    }

    fun setWSPort(value: String): Boolean {
        val port: Int
        try {
            port = value.toInt()
            if (port < 1024) {
                return false
            }
        } catch (e: NumberFormatException) {
            return false
        }

        viewModelScope.launch {
            appPreferences.setWSPort(value.toInt())
        }
        return true
    }

    fun setWSPath(value: String) {
        viewModelScope.launch {
            appPreferences.setWSPath(value)
        }
    }

    fun setAuthEnabled(value: Boolean) {
        viewModelScope.launch {
            appPreferences.setAuthEnabled(value)
        }
    }


    fun setWakeLock(value: Boolean) {
        viewModelScope.launch {
            appPreferences.setWakeLock(value)
        }
    }

    fun setUserName(value: String): Boolean {
        if (value.isBlank()) {
            return false
        }
        viewModelScope.launch {
            appPreferences.setUsername(value)
        }
        return true
    }

    fun setPassword(value: String): Boolean {
        if (value.isBlank()) {
            return false
        }
        viewModelScope.launch {
            appPreferences.setPassword(value)
        }
        return true
    }


}

class SettingsViewModelFactory(private val appPreferences: AppPreferences) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return SettingsViewModel(appPreferences) as T
    }
}
