
package `in`.eswarm.mahati.mqtt.di

import `in`.eswarm.mahati.mqtt.DesktopMqttProvider
import `in`.eswarm.mahati.mqtt.service.MqttControllerContract

/**
 * The Desktop-specific implementation that returns the singleton instance of the controller.
 */
actual fun getMqttController(): MqttControllerContract {
    return DesktopMqttProvider.controller
}
