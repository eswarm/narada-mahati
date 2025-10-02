
package `in`.eswarm.mahati.mqtt.service

import `in`.eswarm.mahati.mqtt.di.getMqttController

/**
 * A common entry point to get the platform-specific instance of the MQTT controller.
 * Common code (ViewModels) will call this function to get a reference to the controller
 * without knowing the underlying platform's implementation details.
 */
fun getMqttControllerProvider(): MqttControllerContract {
    return getMqttController()
}
