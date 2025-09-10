package `in`.eswarm.mahati.navigation

// Define a sealed class for screen routes
sealed class Screen(val route: String) {
    data object Connection : Screen("connection")
    data object Home : Screen("home")
    data object TopicSubscription : Screen("topic_subscription")
    data object Chat : Screen("chat/{topicName}") { // Example: Chat screen needs a topic name argument
        fun createRoute(topicName: String) = "chat/$topicName"
    }
    // Add other screens here as needed
}
