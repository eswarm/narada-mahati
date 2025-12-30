package `in`.eswarm.narada.service

import `in`.eswarm.narada.mqtt.MQTTWrapper
import `in`.eswarm.narada.util.AppContext
import kotlinx.coroutines.flow.StateFlow

class AndroidServerManager(val mqttWrapper: MQTTWrapper) : ServerManager {

    override val isRunning: StateFlow<Boolean>
        get() = mqttWrapper.isRunning

    override val clientsConnected: Int
        get() = mqttWrapper.clientsConnected

    override fun start() {
        MQTTServerService.start(AppContext.context)
    }

    override fun stop() {
        MQTTServerService.stop(AppContext.context)
    }
}

actual fun getServerManager(mqttWrapper: MQTTWrapper): ServerManager {
    return AndroidServerManager(mqttWrapper)
}
