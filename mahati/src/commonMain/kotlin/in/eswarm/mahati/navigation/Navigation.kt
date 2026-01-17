package `in`.eswarm.mahati.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
    @Serializable
    data object Home : Route()

    @Serializable
    data object Settings : Route()

    @Serializable
    data object Log : Route()

    @Serializable
    data object NewConnection : Route()

    @Serializable
    data class EditConnection(val clientID: String) : Route()

    @Serializable
    data object QRCode : Route()

    @Serializable
    data class Chat(val clientID: String, val topicName: String) : Route() {
        fun deepLink(): String {
            return "mahati://chat/$clientID/$topicName"
        }
    }

    @Serializable
    data class TopicSubscription(val clientID: String) : Route()

    @Serializable
    data object NoSelection : Route() // For desktop layout
}



