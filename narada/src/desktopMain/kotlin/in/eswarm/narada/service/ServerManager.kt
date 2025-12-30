package `in`.eswarm.narada.service

import `in`.eswarm.narada.mqtt.MQTTWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DesktopServerManager(val mqttWrapper: MQTTWrapper) : ServerManager {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val clientsConnected: StateFlow<Int>
        get() = mqttWrapper.clientsConnected

    override val isRunning: StateFlow<Boolean>
        get() = mqttWrapper.isRunning

    override fun start() {
        scope.launch {
            val serverProperties = appComponent.appPreferences.getServerProperties()
            mqttWrapper.startMoquette(serverProperties)
        }
    }

    override fun stop() {
        mqttWrapper.stopMoquette()
    }
}

actual fun getServerManager(mqttWrapper: MQTTWrapper): ServerManager {
    return DesktopServerManager(mqttWrapper)
}
