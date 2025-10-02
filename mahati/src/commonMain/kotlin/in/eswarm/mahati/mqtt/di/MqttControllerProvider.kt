
package `in`.eswarm.mahati.mqtt.di

import `in`.eswarm.mahati.mqtt.service.MqttControllerContract

/**
 * This expect function declares that each platform must provide a way
 * to get an instance of the MqttControllerContract.
 */
expect fun getMqttController(): MqttControllerContract
