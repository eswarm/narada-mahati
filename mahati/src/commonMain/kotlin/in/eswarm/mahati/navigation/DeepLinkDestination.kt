package `in`.eswarm.mahati.navigation

sealed class DeepLinkDestination {
    data class Chat(val clientId: String, val topicName: String) : DeepLinkDestination()
}
