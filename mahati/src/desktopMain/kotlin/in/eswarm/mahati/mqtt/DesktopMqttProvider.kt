
package `in`.eswarm.mahati.mqtt

import `in`.eswarm.mahati.mqtt.controller.MqttConnectionController
import `in`.eswarm.mahati.mqtt.service.MqttControllerContract
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

object DesktopMqttProvider {
    // A CoroutineScope for the entire application lifecycle on desktop.
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineName("DesktopAppScope"))

    // The single, lazy-initialized instance of the MQTT controller.
    val controller: MqttControllerContract by lazy {
        MqttConnectionController(applicationScope)
    }

    fun shutdown() {
        controller.shutdownAll()
        applicationScope.cancel() // Cancel the scope to clean up all coroutines
    }
}
