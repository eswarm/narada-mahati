package `in`.eswarm.mahati.navigation

import java.net.URLEncoder

// Define a sealed class for screen routes
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object TopicSubscription : Screen("topic_subscription/{clientID}") {
        fun createRoute(clientID: String) = "topic_subscription/$clientID"
    }

    data object Chat : Screen("chat/{clientID}/{topicName}") {
        fun createRoute(clientID: String, topicName: String): String {
            val encodedClient = URLEncoder.encode(clientID, "UTF-8")
            val encodedTopic = URLEncoder.encode(topicName, "UTF-8")
            return "chat/$encodedClient/$encodedTopic"
        }

        fun createDeepLink(clientID: String, topicName: String): String {
            val encodedClient = URLEncoder.encode(clientID, "UTF-8")
            val encodedTopic = URLEncoder.encode(topicName, "UTF-8")
            return "mahati://chat/$encodedClient/$encodedTopic"
        }

        const val uriPattern = "mahati://chat/{clientID}/{topicName}"
    }

    data object NewConnection : Screen("new_connection")

    data object EditConnection : Screen("edit_connection/{clientID}") {
        fun createRoute(clientID: String) = "edit_connection/$clientID"
    }

    data object Settings : Screen("settings")

    data object ScanQr : Screen("scan_qr")

    data object Log : Screen("log")
}
