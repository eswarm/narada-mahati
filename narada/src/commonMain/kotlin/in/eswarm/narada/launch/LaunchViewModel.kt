package `in`.eswarm.narada.launch

import `in`.eswarm.narada.log.LogStream
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import `in`.eswarm.narada.mqtt.MQTTServerListener
import `in`.eswarm.narada.mqtt.MQTTWrapper
import `in`.eswarm.narada.mqtt.ServerProperties
import `in`.eswarm.narada.preferences.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

open class LaunchViewModel(
    private val logStream: LogStream,
    private val listener: MQTTServerListener,
    private val appPreferences: AppPreferences
) :
    ViewModel() {

    val isServerRunning = MQTTWrapper.isRunning
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
                clientsCount.value = MQTTWrapper.clientsConnected
            }
        }
    }

    fun toggleServer() {
        viewModelScope.launch(Dispatchers.IO) {
            if (isServerRunning.value) {
                appPreferences.setServerStopped()
                MQTTWrapper.stopMoquette()
            } else {
                // Ensure properties are loaded before starting
                if (!::serverProperties.isInitialized) {
                    serverProperties = appPreferences.getServerProperties()
                }
                appPreferences.setServerStarted()
                MQTTWrapper.startMoquette(listener, logStream, serverProperties)
            }
        }
    }

    open fun getLocalIpAddress(): String? = null

    companion object {
        fun Factory(
            logStream: LogStream,
            mqttListener: MQTTServerListener,
            appPreferences: AppPreferences
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: KClass<T>, extras: CreationExtras
            ): T {
                if (modelClass.java.isAssignableFrom(LaunchViewModel::class.java)) {
                    return LaunchViewModel(
                        logStream,
                        mqttListener,
                        appPreferences
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
