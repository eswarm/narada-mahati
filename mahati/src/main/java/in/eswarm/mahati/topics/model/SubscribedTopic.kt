package `in`.eswarm.mahati.topics.model

import java.util.Date

/**
 * Represents a topic the user is currently subscribed to.
 *
 * @param topicFilter The exact topic filter string used for subscription (e.g., "my/device/+/data", "sensors/temp").
 * @param qos The Quality of Service level requested for this subscription.
 * @param subscribedAt Timestamp of when the subscription was made.
 */
data class SubscribedTopic(
    val topicFilter: String,
    val qos: Int,
    val subscribedAt: Date = Date() // Default to current time, can be set explicitly
)
