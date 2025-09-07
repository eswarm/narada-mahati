package `in`.eswarm.mahati.topics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.eswarm.mahati.mqtt.core.MqttManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UI Events
sealed interface TopicSubscriptionEvent {
    data object FabClicked : TopicSubscriptionEvent
    data class SubscribeToTopic(val topicFilter: String, val qos: Int) : TopicSubscriptionEvent
    data class UnsubscribeFromTopic(val topicFilter: String) : TopicSubscriptionEvent
    data object DismissSubscribeDialog : TopicSubscriptionEvent
}

// UI State
data class TopicSubscriptionUiState(
    val subscribedTopics: List<SubscribedTopic> = emptyList(),
    val showSubscribeDialog: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class TopicSubscriptionViewModel(
    private val mqttManager: MqttManager // Inject your MqttManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopicSubscriptionUiState())
    val uiState: StateFlow<TopicSubscriptionUiState> = _uiState.asStateFlow()

    init {
        // In a real app, you might want to load existing subscriptions if MqttManager keeps track
        // Or, this list is purely ephemeral and reflects what this ViewModel has asked to subscribe to.
        // For this example, we'll manage the list within the ViewModel based on subscribe/unsubscribe actions.

        // Example: Observe MqttManager's connection state to clear topics on disconnect
        // viewModelScope.launch {
        //     mqttManager.connectionState.collect { state ->
        //         if (state is MqttClientState.Disconnected || state is MqttClientState.Error) {
        //             _uiState.update { it.copy(subscribedTopics = emptyList(), error = "Disconnected") }
        //         }
        //     }
        // }
    }

    fun onEvent(event: TopicSubscriptionEvent) {
        when (event) {
            is TopicSubscriptionEvent.FabClicked -> {
                _uiState.update { it.copy(showSubscribeDialog = true) }
            }
            is TopicSubscriptionEvent.DismissSubscribeDialog -> {
                _uiState.update { it.copy(showSubscribeDialog = false) }
            }
            is TopicSubscriptionEvent.SubscribeToTopic -> {
                handleSubscribeToTopic(event.topicFilter, event.qos)
            }
            is TopicSubscriptionEvent.UnsubscribeFromTopic -> {
                handleUnsubscribeFromTopic(event.topicFilter)
            }
        }
    }

    private fun handleSubscribeToTopic(topicFilter: String, qos: Int) {
        if (topicFilter.isBlank()) {
            _uiState.update { it.copy(error = "Topic filter cannot be empty", showSubscribeDialog = false) }
            return
        }
        _uiState.update { it.copy(isLoading = true, showSubscribeDialog = false) }
        viewModelScope.launch {
            val success = mqttManager.subscribe(topicFilter, qos)
            if (success) {
                val newSubscription = SubscribedTopic(topicFilter, qos)
                _uiState.update { currentState ->
                    // Avoid duplicates if already present (though MQTT broker handles actual subscription state)
                    if (currentState.subscribedTopics.any { it.topicFilter == topicFilter }) {
                        currentState.copy(isLoading = false, error = "Already subscribed to $topicFilter (or re-subscribed)")
                    } else {
                        currentState.copy(
                            subscribedTopics = currentState.subscribedTopics + newSubscription,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Failed to subscribe to $topicFilter") }
            }
        }
    }

    private fun handleUnsubscribeFromTopic(topicFilter: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val success = mqttManager.unsubscribe(topicFilter)
            // Even if unsubscribe call fails, we remove it from UI list as intent is to unsubscribe.
            // Broker state is the source of truth.
            _uiState.update { currentState ->
                currentState.copy(
                    subscribedTopics = currentState.subscribedTopics.filterNot { it.topicFilter == topicFilter },
                    isLoading = false,
                    error = if (!success) "Failed to send unsubscribe for $topicFilter to broker" else null
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
