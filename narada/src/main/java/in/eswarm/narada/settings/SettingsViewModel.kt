package `in`.eswarm.narada.settings

import `in`.eswarm.narada.preferences.AppPreferences
import `in`.eswarm.narada.util.preferences
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

class SettingsViewModelFactory(private val context: Context) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(context.preferences) as T
    }
}
