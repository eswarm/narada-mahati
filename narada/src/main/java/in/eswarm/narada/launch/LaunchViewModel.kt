package `in`.eswarm.narada.launch

import `in`.eswarm.narada.log.LogStream
import `in`.eswarm.narada.log.MsgType
import `in`.eswarm.narada.mqtt.MQTTService
import `in`.eswarm.narada.mqtt.MQTTWrapper
import `in`.eswarm.narada.util.NetworkUtil
import `in`.eswarm.narada.util.getAppComponent
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import `in`.eswarm.narada.util.preferences
import kotlinx.coroutines.launch

class LaunchViewModel(private val logStream: LogStream) : ViewModel() {

    var isServerRunning = mutableStateOf(MQTTService.isMoquetteRunning)
    var logs = mutableStateListOf<String>()
    var clientsCount = mutableStateOf(MQTTWrapper.clientsConnected)

    init {
        viewModelScope.launch {
            logStream.logFlow.collect { logData ->
                if (logData.msgType == MsgType.CONNECTION) {
                    clientsCount.value = MQTTWrapper.clientsConnected
                }
                logs.add(logData.msg + "\n")
            }
        }
    }

    private fun startServer(context: Context) {
        isServerRunning.value = true
        MQTTService.start(context)
    }

    private fun stopServer(context: Context) {
        isServerRunning.value = false
        MQTTService.stop(context)
    }

    fun getLocalIpAddress(): String? {
        return NetworkUtil.getLocalIpAddress()
    }

    fun toggleServer(context: Context) {
        if (isServerRunning.value) {
            stopServer(context)
            viewModelScope.launch {
                context.preferences.setServerStopped()
            }
        } else {
            startServer(context)
            viewModelScope.launch {
                context.preferences.setServerStarted()
            }
        }
    }

    fun serverStatus(): String {
        return if (isServerRunning.value) {
            "Running"
        } else {
            "Stopped"
        }
    }
}

class LaunchViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val logStream = context.getAppComponent().logStream
        return LaunchViewModel(logStream) as T
    }
}