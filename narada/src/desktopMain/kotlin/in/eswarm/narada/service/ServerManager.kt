package `in`.eswarm.narada.service

import `in`.eswarm.narada.mqtt.MQTTWrapper
import `in`.eswarm.narada.preferences.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DesktopServerManager(val mqttWrapper: MQTTWrapper, val appPreferences: AppPreferences) : ServerManager {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val clientsConnected: StateFlow<Int>
        get() = mqttWrapper.clientsConnected

    override val isRunning: StateFlow<Boolean>
        get() = mqttWrapper.isRunning

    override fun start() {
        scope.launch {
            val serverProperties = appPreferences.getServerProperties()
            mqttWrapper.startMoquette(serverProperties)
        }
    }

    override fun stop() {
        mqttWrapper.stopMoquette()
    }
}

actual fun getServerManager(mqttWrapper: MQTTWrapper, appPreferences: AppPreferences): ServerManager {
    return DesktopServerManager(mqttWrapper, appPreferences)
}
