package `in`.eswarm.mahati.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MessageRepository {
    private val queries = getMahatiDb().appMqttMessageQueries

    suspend fun insertMessage(
        clientID: String,
        topicName: String,
        payload: ByteArray,
        qos: Long, // Matches INTEGER
        retained: Boolean, // Will be handled by booleanAdapter
        direction: MessageDirection, // Will be handled by messageDirectionAdapter
        timestamp: Long // Matches INTEGER
    ) {
        withContext(Dispatchers.IO) {
            queries.insertMessage(
                clientID = clientID,
                topicName = topicName,
                payload = payload,
                qos = qos,
                retained = retained,
                direction = direction,
                timestamp = timestamp
            )
        }
    }

    suspend fun getMessagesByClientIdAndTopic(
        clientID: String,
        topicName: String
    ): List<AppMqttMessage> {
        return withContext(Dispatchers.IO) {
            queries.getMessagesByClientIdAndTopic(clientID, topicName).executeAsList()
        }
    }

    fun getMessagesByClientIdAndTopicFlow(
        clientID: String,
        topicName: String
    ): Flow<List<AppMqttMessage>> {
        return queries.getMessagesByClientIdAndTopic(clientID, topicName)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    suspend fun getMessagesByClientId(clientID: String): List<AppMqttMessage> {
        return withContext(Dispatchers.IO) {
            queries.getMessagesByClientId(clientID).executeAsList()
        }
    }

    fun getMessagesByClientIdFlow(clientID: String): Flow<List<AppMqttMessage>> {
        return queries.getMessagesByClientId(clientID).asFlow().mapToList(Dispatchers.IO)
    }

    suspend fun deleteMessagesOlderThan(timestamp: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteMessagesOlderThan(timestamp)
        }
    }

    suspend fun deleteAllMessagesByClientId(clientID: String) {
        withContext(Dispatchers.IO) {
            queries.deleteMessagesByClientId(clientID)
        }
    }

    suspend fun deleteAllMessages() {
        withContext(Dispatchers.IO) {
            queries.deleteAllMessages()
        }
    }
}
