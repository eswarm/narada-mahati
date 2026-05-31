package `in`.eswarm.mahati.navigation

import kotlinx.serialization.Serializable
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
        /**
         * TODO [BUG-5 FIX]: The old URI put topicName directly in the path, e.g.
         *   mahati://chat/myClient/home/temp
         * The Android URI parser splits path segments on '/', so pathSegments would be
         *   ["myClient", "home", "temp"] — clientId was lost and topicName was only "home".
         * Fix: URL-encode the topicName so '/' becomes '%2F', keeping the path as exactly
         * two segments: [clientID, encodedTopicName].
         * HomeActivity.parseDeepLink() is updated to URL-decode the topicName on receipt.
         */
        fun deepLink(): String {
            val encodedTopic = URLEncoder.encode(topicName, StandardCharsets.UTF_8.name())
            return "mahati://chat/$clientID/$encodedTopic"
        }
    }

    @Serializable
    data class TopicSubscription(val clientID: String) : Route()

    @Serializable
    data object NoSelection : Route() // For desktop layout
}



