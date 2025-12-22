package `in`.eswarm.narada.service

import `in`.eswarm.narada.AppComponent
import `in`.eswarm.narada.util.AppContext
import kotlinx.coroutines.flow.StateFlow

class AndroidServerManager : ServerManager {

    override val isRunning: StateFlow<Boolean>
        get() = AppComponent.INSTANCE.mqttWrapper.isRunning

    override val clientsConnected: Int
        get() = AppComponent.INSTANCE.mqttWrapper.clientsConnected

    override fun start() {
        MQTTServerService.start(AppContext.context)
    }

    override fun stop() {
        MQTTServerService.stop(AppContext.context)
    }
}

actual fun getServerManager(): ServerManager {
    return AndroidServerManager()
}
