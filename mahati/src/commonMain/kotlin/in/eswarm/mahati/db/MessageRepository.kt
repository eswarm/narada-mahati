package `in`.eswarm.mahati.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO // For Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MessageRepository {

    // Assuming SQLDelight generates 'mqttMessageQueries' based on 'MqttMessage.sq'
    // and the table name (e.g., MqttMessage or MqttMessageEntity)
    private val queries = getMahatiDb().mqttMessageQueries

    suspend fun storeMessage(
        clientId: String,
        topicName: String,
        payload: ByteArray,
        qos: Long, // Matches INTEGER
        retained: Boolean, // Will be handled by booleanAdapter
        direction: MessageDirection, // Will be handled by messageDirectionAdapter
        timestamp: Long // Matches INTEGER
    ) {
        withContext(Dispatchers.IO) {
            queries.insertMessage(
                clientId = clientId,
                topicName = topicName,
                payload = payload,
                qos = qos,
                retained = retained,
                direction = direction,
                timestamp = timestamp
            )
        }
    }

    suspend fun getMessagesByClientIdAndTopic(clientId: String, topicName: String): List<MqttMessage> {
        return withContext(Dispatchers.IO) {
            queries.getMessagesByClientIdAndTopic(clientId, topicName).executeAsList()
        }
    }

    fun getMessagesByClientIdAndTopicFlow(clientId: String, topicName: String): Flow<List<MqttMessage>> {
        return queries.getMessagesByClientIdAndTopic(clientId, topicName)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    suspend fun getMessagesByClientId(clientId: String): List<MqttMessage> {
        return withContext(Dispatchers.IO) {
            queries.getMessagesByClientId(clientId).executeAsList()
        }
    }

    fun getMessagesByClientIdFlow(clientId: String): Flow<List<MqttMessage>> {
        return queries.getMessagesByClientId(clientId).asFlow().mapToList(Dispatchers.IO)
    }

    suspend fun deleteMessagesOlderThan(timestamp: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteMessagesOlderThan(timestamp)
        }
    }

    suspend fun deleteAllMessagesByClientId(clientId: String) {
        withContext(Dispatchers.IO) {
            queries.deleteMessagesByClientId(clientId)
        }
    }

    suspend fun deleteAllMessages() {
        withContext(Dispatchers.IO) {
            queries.deleteAllMessages()
        }
    }
}
