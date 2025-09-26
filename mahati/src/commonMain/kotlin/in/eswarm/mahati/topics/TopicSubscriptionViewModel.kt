package `in`.eswarm.mahati.topics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import `in`.eswarm.mahati.db.SubscribedTopic
import `in`.eswarm.mahati.db.SubscriptionRepository
import `in`.eswarm.mahati.mqtt.core.MqttManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

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
    private val mqttManager: MqttManager,
    private val clientID: String,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopicSubscriptionUiState())
    val uiState: StateFlow<TopicSubscriptionUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val subscriptions = subscriptionRepository.getSubscriptionsByClientId(clientID)

            _uiState.update { currentState ->
                currentState.copy(
                    subscribedTopics = subscriptions, isLoading = false, error = null
                )
            }

        }
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
            _uiState.update {
                it.copy(
                    error = "Topic filter cannot be empty", showSubscribeDialog = false
                )
            }
            return
        }
        _uiState.update { it.copy(isLoading = true, showSubscribeDialog = false) }
        viewModelScope.launch {
            val success = mqttManager.subscribe(topicFilter, qos)
            if (success) {
                val newSubscription = SubscribedTopic(
                    0, clientID, topicFilter, qos.toLong(), System.currentTimeMillis()
                )
                if (uiState.value.subscribedTopics.any { it.topicFilter == topicFilter }) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            error = "Already subscribed to $topicFilter (or re-subscribed)"
                        )
                    }
                } else {
                    subscriptionRepository.insertSubscription(newSubscription)
                    load()
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false, error = "Failed to subscribe to $topicFilter"
                    )
                }
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

class TopicViewModelFactory(
    val mqttManager: MqttManager,
    val clientID: String,
    val subscriptionRepository: SubscriptionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: KClass<T>, extras: CreationExtras
    ): T {
        return TopicSubscriptionViewModel(mqttManager, clientID, subscriptionRepository) as T
    }

}
