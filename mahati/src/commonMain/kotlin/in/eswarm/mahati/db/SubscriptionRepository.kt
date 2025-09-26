package `in`.eswarm.mahati.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SubscriptionRepository {

    // Assuming SQLDelight generates 'subscribedTopicQueries' based on 'SubscribedTopic.sq'
    // and the table name (e.g., SubscribedTopic or SubscribedTopicEntity)
    private val queries = getMahatiDb().subscribedTopicQueries

    suspend fun insertSubscription(
        clientID: String,
        topicFilter: String,
        qos: Long, // Matches INTEGER in .sq
        subscribedAt: Long // Matches INTEGER in .sq
    ) {
        withContext(Dispatchers.IO) {
            queries.insertSubscription(
                clientID = clientID,
                topicFilter = topicFilter,
                qos = qos,
                subscribedAt = subscribedAt
            )
        }
    }

    suspend fun insertSubscription(subscription: SubscribedTopic) {
        insertSubscription(
            clientID = subscription.clientID,
            topicFilter = subscription.topicFilter,
            qos = subscription.qos,
            subscribedAt = subscription.subscribedAt
        )
    }

    suspend fun getSubscriptionsByClientId(clientID: String): List<SubscribedTopic> {
        return withContext(Dispatchers.IO) {
            queries.getSubscriptionsByClientId(clientID).executeAsList()
        }
    }

    fun getSubscriptionsByClientIdFlow(clientID: String): Flow<List<SubscribedTopic>> {
        return queries.getSubscriptionsByClientId(clientID).asFlow().mapToList(Dispatchers.IO)
    }

    suspend fun getSubscription(clientID: String, topicFilter: String): SubscribedTopic? {
        return withContext(Dispatchers.IO) {
            queries.getSubscriptionByClientIdAndTopicFilter(clientID, topicFilter).executeAsOneOrNull()
        }
    }

    suspend fun deleteSubscription(clientID: String, topicFilter: String) {
        withContext(Dispatchers.IO) {
            queries.deleteSubscriptionByClientIdAndTopicFilter(clientID, topicFilter)
        }
    }

    suspend fun deleteAllSubscriptionsByClientId(clientID: String) {
        withContext(Dispatchers.IO) {
            queries.deleteAllSubscriptionsByClientId(clientID)
        }
    }

    suspend fun deleteAllSubscriptions() {
        withContext(Dispatchers.IO) {
            queries.deleteAllSubscriptions()
        }
    }
}
