package `in`.eswarm.mahati.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO // For Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SubscriptionRepository {

    // Assuming SQLDelight generates 'subscribedTopicQueries' based on 'SubscribedTopic.sq'
    // and the table name (e.g., SubscribedTopic or SubscribedTopicEntity)
    private val queries = getMahatiDb().subscribedTopicQueries

    suspend fun addSubscription(
        clientId: String,
        topicFilter: String,
        qos: Long, // Matches INTEGER in .sq
        subscribedAt: Long // Matches INTEGER in .sq
    ) {
        withContext(Dispatchers.IO) {
            queries.insertSubscription(
                clientId = clientId,
                topicFilter = topicFilter,
                qos = qos,
                subscribedAt = subscribedAt
            )
        }
    }

    suspend fun getSubscriptionsByClientId(clientId: String): List<SubscribedTopic> {
        return withContext(Dispatchers.IO) {
            queries.getSubscriptionsByClientId(clientId).executeAsList()
        }
    }

    fun getSubscriptionsByClientIdFlow(clientId: String): Flow<List<SubscribedTopic>> {
        return queries.getSubscriptionsByClientId(clientId).asFlow().mapToList(Dispatchers.IO)
    }

    suspend fun getSubscription(clientId: String, topicFilter: String): SubscribedTopic? {
        return withContext(Dispatchers.IO) {
            queries.getSubscriptionByClientIdAndTopicFilter(clientId, topicFilter).executeAsOneOrNull()
        }
    }

    suspend fun deleteSubscription(clientId: String, topicFilter: String) {
        withContext(Dispatchers.IO) {
            queries.deleteSubscriptionByClientIdAndTopicFilter(clientId, topicFilter)
        }
    }

    suspend fun deleteAllSubscriptionsByClientId(clientId: String) {
        withContext(Dispatchers.IO) {
            queries.deleteAllSubscriptionsByClientId(clientId)
        }
    }

    suspend fun deleteAllSubscriptions() {
        withContext(Dispatchers.IO) {
            queries.deleteAllSubscriptions()
        }
    }
}
