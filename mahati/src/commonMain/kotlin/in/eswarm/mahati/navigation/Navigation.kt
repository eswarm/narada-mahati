package `in`.eswarm.mahati.navigation

// Define a sealed class for screen routes
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object TopicSubscription : Screen("topic_subscription/{clientID}") {
        fun createRoute(clientID: String) = "topic_subscription/$clientID"
    }
    data object Chat : Screen("chat/{clientID}/{topicName}") {
        fun createRoute(clientID: String, topicName: String) = "chat/$clientID/$topicName"
    }

    data object NewConnection: Screen("new_connection")
}
