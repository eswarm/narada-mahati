package `in`.eswarm.mahati.topics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import `in`.eswarm.mahati.db.SubscribedTopic
import `in`.eswarm.mahati.db.SubscriptionRepository
import `in`.eswarm.mahati.mqtt.service.MqttControllerContract
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
    private val mqttController: MqttControllerContract,
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
            val success =
                mqttController.subscribe(clientID = clientID, topicFilter = topicFilter, qos = qos)
            if (success == true) {
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
            val success = mqttController.unsubscribe(clientID, topicFilter)

            // if unsubscribe call fails, we keep it in UI
            _uiState.update { currentState ->
                currentState.copy(
                    subscribedTopics =
                        // will be updated in the load below
                        currentState.subscribedTopics,
                    isLoading = false,
                    error = if (success == false) {
                        "Failed to send unsubscribe for $topicFilter to broker"
                    } else {
                        null
                    }
                )
            }

            if (success == true) {
                subscriptionRepository.deleteSubscription(clientID, topicFilter)
                load()
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

class TopicViewModelFactory(
    val mqttController: MqttControllerContract,
    val clientID: String,
    val subscriptionRepository: SubscriptionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: KClass<T>, extras: CreationExtras
    ): T {
        return TopicSubscriptionViewModel(mqttController, clientID, subscriptionRepository) as T
    }

}
