package `in`.eswarm.mahati.mqtt.common

import `in`.eswarm.mahati.db.AppMqttMessage

/**
 * Represents the various states of the MQTT client.
 */
sealed interface MqttClientState {
    data object Disconnected :
        MqttClientState // Client is disconnected or connection attempt was cancelled.

    data object Connecting : MqttClientState   // Client is attempting to connect.
    data class Connected(val serverUri: String, val clientID: String) :
        MqttClientState // Client is successfully connected.

    data class Error(val message: String, val cause: Throwable? = null) :
        MqttClientState // An error occurred.
}

val AppMqttMessage.payloadAsText: String
    get() = String(this.payload, Charsets.UTF_8)