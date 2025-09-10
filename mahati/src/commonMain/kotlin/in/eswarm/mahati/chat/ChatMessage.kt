package `in`.eswarm.mahati.chat

import java.util.UUID
import java.util.Date

/**
 * Represents a single message in the chat UI.
 *
 * @param id Unique identifier for the message.
 * @param text The content of the message.
 * @param timestamp The time the message was sent or received.
 * @param senderId Identifier for the sender of the message.
 * @param isSentByUser True if this message was sent by the current user, false otherwise.
 * @param status Status of the message if sent by the current user. Null for received messages.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val timestamp: Date = Date(),
    val senderId: String, // To identify the sender (could be current user or other user)
    val isSentByUser: Boolean,
    val status: MessageStatus? = if (isSentByUser) MessageStatus.SENDING else null
)