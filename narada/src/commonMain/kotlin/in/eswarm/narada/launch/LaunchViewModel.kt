package `in`.eswarm.narada.launch

import `in`.eswarm.narada.share.ConnectionDetails
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import `in`.eswarm.narada.mqtt.ServerProperties
import `in`.eswarm.narada.preferences.AppPreferences
import `in`.eswarm.narada.service.ServerManager
import `in`.eswarm.shared.LogStream
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

open class LaunchViewModel(
    private val logStream: LogStream,
    private val appPreferences: AppPreferences,
    private val serverManager: ServerManager
) :
    ViewModel() {

    val isServerRunning = serverManager.isRunning
    var logs = mutableStateListOf<String>()
    var clientsCount = mutableStateOf(0)
    lateinit var serverProperties: ServerProperties

    init {
        viewModelScope.launch {
            // Concurrently load server properties
            serverProperties = appPreferences.getServerProperties()

            // Collect the logs from the persistent DataStore
            logStream.logFlow.collect { logDataList ->
                logs.clear()
                logs.addAll(logDataList.map { it.msg + "\n" })
                clientsCount.value = serverManager.clientsConnected
            }
        }
    }

    fun getConnectionString(): String? {
        val host = getLocalIpAddress() ?: return null
        if (!::serverProperties.isInitialized) return null

        val details = ConnectionDetails(
            host = host,
            port = serverProperties.mqttPort,
            wsEnabled = serverProperties.wsEnabled,
            wsPort = serverProperties.wsPort,
            wsPath = serverProperties.wsPath,
            authEnabled = serverProperties.authEnabled,
            username = serverProperties.userName,
            password = serverProperties.password
        )
        return Json.encodeToString(details)
    }

    fun toggleServer() {
        if (isServerRunning.value) {
            serverManager.stop()
        } else {
            serverManager.start()
        }
    }

    open fun getLocalIpAddress(): String? = `in`.eswarm.narada.util.getLocalIpAddress()
    fun clearLogs() {
        logStream.clear()
    }

    companion object {
        fun Factory(
            logStream: LogStream,
            appPreferences: AppPreferences,
            serverManager: ServerManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: KClass<T>, extras: CreationExtras
            ): T {
                if (modelClass.java.isAssignableFrom(LaunchViewModel::class.java)) {
                    return LaunchViewModel(
                        logStream,
                        appPreferences,
                        serverManager
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
