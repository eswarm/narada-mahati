package `in`.eswarm.mahati.chat.model

/**
 * Represents the status of a sent chat message.
 */
enum class MessageStatus {
    SENDING, // Message is currently being sent
    SENT,    // Message was successfully published to the MQTT broker
    FAILED   // Message failed to send
    // Future: DELIVERED (would require QoS 1/2 acks and potentially custom logic)
    // Future: READ (would require a custom read receipt mechanism)
}