package `in`.eswarm.mahati.navigation

import kotlinx.serialization.Serializable

sealed class Route {
    @Serializable
    object Home

    @Serializable
    object Settings

    @Serializable
    object Log

    @Serializable
    object NewConnection

    @Serializable
    data class EditConnection(val clientID: String)

    @Serializable
    object QRCode

    @Serializable
    data class Chat(val clientID: String, val topicName: String) {

        fun deepLink(): String {
            return "mahati://chat/$clientID/$topicName"
        }
    }

    @Serializable
    data class TopicSubscription(val clientID: String)
}
