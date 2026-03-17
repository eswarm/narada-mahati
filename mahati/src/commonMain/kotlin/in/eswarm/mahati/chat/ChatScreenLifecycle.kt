package `in`.eswarm.mahati.chat

import kotlinx.coroutines.flow.MutableStateFlow
import `in`.eswarm.mahati.mqtt.common.mqttTopicMatches

/**
 * ChatScreenLifecycle now tracks the active clientID and topicFilter
 * instead of just a boolean. This ensures notifications are only suppressed for the
 * exact topic/client combination that is currently open, not all topics globally.
 */
object ChatScreenLifecycle {
    private val _activeChatScreen = MutableStateFlow<ActiveChat?>(null)

    /**
     * Returns true if a notification SHOULD be shown (i.e. this message is NOT for the
     * currently open chat screen).
     */
    fun shouldShowNotification(clientID: String, topicName: String): Boolean {
        val active = _activeChatScreen.value ?: return true
        // Suppress only when the open chat's filter matches this incoming message topic
        return !(active.clientID == clientID && mqttTopicMatches(active.topicFilter, topicName))
    }

    fun onChatScreenVisible(clientID: String, topicFilter: String) {
        _activeChatScreen.value = ActiveChat(clientID, topicFilter)
    }

    fun onChatScreenHidden() {
        _activeChatScreen.value = null
    }

    data class ActiveChat(val clientID: String, val topicFilter: String)
}
