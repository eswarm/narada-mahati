package `in`.eswarm.mahati.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import `in`.eswarm.mahati.mqtt.common.MqttClientState
import `in`.eswarm.mahati.mqtt.core.MqttManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

// A simple structure for messages on the wire.
// In a real app, you'd use a robust serialization format like JSON (e.g., with kotlinx.serialization).
data class WireMessage(val senderId: String, val text: String, val timestamp: Long = System.currentTimeMillis())

// Helper to (de)serialize. Replace with proper JSON library for production.
fun WireMessage.toJsonString(): String = "${'$'}{senderId}|${'$'}{timestamp}|${'$'}{text}"
fun String.toWireMessage(): WireMessage? {
    val parts = this.split("|", limit = 3)
    return if (parts.size == 3) {
        try {
            WireMessage(parts[0], parts[2], parts[1].toLong())
        } catch (e: Exception) {
            null // Malformed
        }
    } else null
}


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

    init {
        viewModelScope.launch {
            // Subscribe to the chat topic if not already
            // This assumes MqttManager handles multiple subscriptions correctly
            // or this screen is only active when already subscribed.
            // For robust behavior, ensure subscription happens before trying to receive.
            val subscribed = mqttManager.subscribe(chatTopic, qos = 1) // Use QoS 1 for more reliability
            if (!subscribed) {
                _uiState.update { it.copy(error = "Failed to subscribe to chat topic: $chatTopic") }
            }
        }

        viewModelScope.launch {
            mqttManager.connectionState.collect { state ->
                _uiState.update { it.copy(isConnected = state is MqttClientState.Connected) }
            }
        }

        viewModelScope.launch {
            mqttManager.receivedMessages.collect { receivedMqttMessage ->
                if (receivedMqttMessage.topic == chatTopic) {
                    val wireMessage = receivedMqttMessage.payloadAsText.toWireMessage()
                    if (wireMessage != null && wireMessage.senderId != currentUserId) {
                        val chatMsg = ChatMessage(
                            text = wireMessage.text,
                            timestamp = Date(wireMessage.timestamp),
                            senderId = wireMessage.senderId,
                            isSentByUser = false
                        )
                        _uiState.update { currentState ->
                            currentState.copy(messages = currentState.messages + chatMsg)
                        }
                    } else if (wireMessage == null) {
                        // Handle potential non-WireMessage format if topic is shared
                        // For a dedicated chat topic, this might indicate an issue or different message type
                        println("Received malformed message on $chatTopic: ${receivedMqttMessage.payloadAsText}")
                    }
                }
            }
        }
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
            val wireMessage = WireMessage(senderId = currentUserId, text = inputText)
            val success = mqttManager.publish(chatTopic, wireMessage.toJsonString(), qos = 1, retain = false)
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
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                    return ChatViewModel(mqttManager, chatTopic, currentUserId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
