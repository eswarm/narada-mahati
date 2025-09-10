package `in`.eswarm.mahati.home

import `in`.eswarm.mahati.mqtt.common.MqttConnectionParams

/**
 * Represents a saved MQTT connection profile for the UI.
 *
 * @param id A unique identifier for this profile.
 * @param name A user-friendly name for this connection (e.g., "Home Broker", "Test Server").
 * @param params The actual parameters needed to establish the MQTT connection.
 */
data class MqttConnectionProfile(
    val id: String,
    val name: String,
    val params: MqttConnectionParams
)