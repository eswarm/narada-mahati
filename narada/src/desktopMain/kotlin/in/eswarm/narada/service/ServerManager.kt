package `in`.eswarm.narada.service

import `in`.eswarm.narada.AppComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DesktopServerManager : ServerManager {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val clientsConnected: Int
        get() = AppComponent.INSTANCE.mqttWrapper.clientsConnected

    override val isRunning: StateFlow<Boolean>
        get() = AppComponent.INSTANCE.mqttWrapper.isRunning

    override fun start() {
        scope.launch {
            val appComponent = AppComponent.INSTANCE
            val serverProperties = appComponent.appPreferences.getServerProperties()
            appComponent.mqttWrapper.startMoquette(serverProperties)
        }
    }

    override fun stop() {
        AppComponent.INSTANCE.mqttWrapper.stopMoquette()
    }
}

actual fun getServerManager(): ServerManager {
    return DesktopServerManager()
}
