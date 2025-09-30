package `in`.eswarm.mahati.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import `in`.eswarm.mahati.mqtt.common.MqttClientState
import `in`.eswarm.mahati.mqtt.common.payloadAsText
import `in`.eswarm.mahati.mqtt.core.MqttManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.reflect.KClass

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentInput: String = "",
    val isConnected: Boolean = false,
    val error: String? = null
)

class ChatViewModel(
    private val mqttManager: MqttManager,
    val chatTopic: String, // Topic for this specific chat
    private val currentUserId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    val connectionState = mqttManager.connectionState

    init {
        viewModelScope.launch {
            // Subscribe to the chat topic if not already
            // This assumes MqttManager handles multiple subscriptions correctly
            // or this screen is only active when already subscribed.
            // For robust behavior, ensure subscription happens before trying to receive.
            val subscribed =
                mqttManager.subscribe(chatTopic, qos = 1) // Use QoS 1 for more reliability
            if (!subscribed) {
                _uiState.update { it.copy(error = "Failed to subscribe to chat topic: $chatTopic") }
            }
        }

        viewModelScope.launch {
            mqttManager.receivedMessages.collect { receivedMqttMessage ->
                if (receivedMqttMessage.topicName == chatTopic) {
                    if (receivedMqttMessage.clientID != currentUserId) {
                        val chatMsg = ChatMessage(
                            text = receivedMqttMessage.payloadAsText,
                            timestamp = System.currentTimeMillis(),
                            senderId = receivedMqttMessage.clientID,
                            isSentByUser = false
                        )
                        _uiState.update { currentState ->
                            currentState.copy(messages = currentState.messages + chatMsg)
                        }
                    } else if (receivedMqttMessage == null) {
                        // Handle potential non-WireMessage format if topic is shared
                        // For a dedicated chat topic, this might indicate an issue or different message type
                        println("Received malformed message on $chatTopic: ${receivedMqttMessage.payloadAsText}")
                    }
                }
            }
        }
    }

    fun onMqttStateChange(state: MqttClientState) {
        _uiState.update { it.copy(isConnected = state is MqttClientState.Connected) }
    }

    fun onInputChange(newInput: String) {
        _uiState.update { it.copy(currentInput = newInput) }
    }

    fun sendMessage() {
        val inputText = _uiState.value.currentInput.trim()
        if (inputText.isEmpty() || !_uiState.value.isConnected) return

        val tempMessageId = UUID.randomUUID().toString()
        val chatMessage = ChatMessage(
            id = tempMessageId,
            text = inputText,
            senderId = currentUserId,
            isSentByUser = true,
            status = MessageStatus.SENDING
        )

        _uiState.update {
            it.copy(
                messages = it.messages + chatMessage,
                currentInput = "" // Clear input field
            )
        }

        viewModelScope.launch {
            val success =
                mqttManager.publish(chatTopic, inputText, qos = 1, retain = false)
            val finalStatus = if (success) MessageStatus.SENT else MessageStatus.FAILED

            _uiState.update { currentState ->
                currentState.copy(
                    messages = currentState.messages.map { msg ->
                        if (msg.id == tempMessageId) msg.copy(status = finalStatus) else msg
                    }
                )
            }
            if (!success) {
                _uiState.update { it.copy(error = "Failed to send message.") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun Factory(
            mqttManager: MqttManager,
            chatTopic: String,
            currentUserId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")

            override fun <T : ViewModel> create(
                modelClass: KClass<T>,
                extras: CreationExtras
            ): T {
                if (modelClass.java.isAssignableFrom(ChatViewModel::class.java)) {
                    return ChatViewModel(mqttManager, chatTopic, currentUserId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")

            }
        }
    }
}
