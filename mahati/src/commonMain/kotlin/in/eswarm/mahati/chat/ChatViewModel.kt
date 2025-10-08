package `in`.eswarm.mahati.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import `in`.eswarm.mahati.db.MessageDirection
import `in`.eswarm.mahati.db.MessageRepository
import `in`.eswarm.mahati.mqtt.common.MqttClientState
import `in`.eswarm.mahati.mqtt.common.payloadAsText
import `in`.eswarm.mahati.mqtt.service.MqttControllerContract
import kotlinx.coroutines.Dispatchers
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
    private val mqttController: MqttControllerContract,
    private val clientID: String,
    val topic: String,
    val messageRepo: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    val connectionState = mqttController.connectionStatesMap

    init {
        viewModelScope.launch {
            // Subscribe to the chat topic if not already
            // This assumes MqttManager handles multiple subscriptions correctly
            // or this screen is only active when already subscribed.
            // For robust behavior, ensure subscription happens before trying to receive.
            val subscribed = mqttController.subscribe(
                clientID = clientID, topicFilter = topic, qos = 1
            ) // Use QoS 1 for more reliability
            if (subscribed != true) {
                _uiState.update { it.copy(error = "Failed to subscribe to chat topic: $topic") }
            }
        }

        viewModelScope.launch {
            mqttController.allMessages?.collect { received ->
                val clientID = received.first
                val message = received.second

                if (message.topicName == topic) {
                    if (message.publisherID != clientID) {
                        val chatMsg = ChatMessage(
                            text = message.payloadAsText,
                            timestamp = System.currentTimeMillis(),
                            senderId = message.publisherID,
                            isSentByUser = false
                        )
                        _uiState.update { currentState ->
                            currentState.copy(messages = currentState.messages + chatMsg)
                        }
                    } else if (received == null) {
                        // Handle potential non-WireMessage format if topic is shared
                        // For a dedicated chat topic, this might indicate an issue or different message type
                        println("Received malformed message on $topic: ${received.payloadAsText}")
                    }
                }
            }
        }

        with(Dispatchers.IO) {
            viewModelScope.launch {
                val messages = messageRepo.getMessagesByClientId(clientID)
                val chatMessages = messages.map { message ->
                    ChatMessage(
                        text = message.payloadAsText,
                        senderId = message.publisherID,
                        isSentByUser = message.direction == MessageDirection.SENT,
                        status = if (message.direction == MessageDirection.SENT) MessageStatus.SENT else null,
                        timestamp = message.timestamp
                    )
                }

                _uiState.update {
                    it.copy(
                        messages = it.messages + chatMessages
                    )
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
            senderId = clientID,
            isSentByUser = true,
            status = MessageStatus.SENDING
        )

        _uiState.update {
            it.copy(
                messages = it.messages + chatMessage, currentInput = "" // Clear input field
            )
        }

        viewModelScope.launch {
            val success = mqttController.publish(
                clientID = clientID,
                topic = topic,
                payload = inputText.toByteArray(),
                qos = 1,
                retain = false
            )

            if (success == true) {
                mqttController.allMessages
            }

            val finalStatus = if (success == true) MessageStatus.SENT else MessageStatus.FAILED

            _uiState.update { currentState ->
                currentState.copy(
                    messages = currentState.messages.map { msg ->
                        if (msg.id == tempMessageId) msg.copy(status = finalStatus) else msg
                    })
            }
            if (success != true) {
                _uiState.update { it.copy(error = "Failed to send message.") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun Factory(
            mqttController: MqttControllerContract,
            clientID: String,
            topic: String,
            messageRepository: MessageRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")

            override fun <T : ViewModel> create(
                modelClass: KClass<T>, extras: CreationExtras
            ): T {
                if (modelClass.java.isAssignableFrom(ChatViewModel::class.java)) {
                    return ChatViewModel(mqttController, clientID, topic, messageRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
