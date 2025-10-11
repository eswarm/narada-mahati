package `in`.eswarm.mahati.db // Assuming this is where you want your repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ConnectionAdapter {

    private val queries = getMahatiDb().mqttConnectionParamsQueries

    suspend fun addConnection(
        brokerHost: String,
        brokerPort: Long,
        clientID: String,
        username: String?,
        password: ByteArray?,
        useSsl: Boolean,
        topicPrefix: String
    ) {
        withContext(Dispatchers.IO) { // Perform DB operation on IO dispatcher
            queries.insert(
                brokerHost = brokerHost,
                brokerPort = brokerPort,
                clientID = clientID,
                username = username,
                password = password,
                useSsl = useSsl, // Corrected: Pass Boolean directly, adapter handles it
                topicPrefix = topicPrefix
            )
        }
    }

    suspend fun getConnectionByClientId(clientID: String): MqttConnection? {
        return withContext(Dispatchers.IO) {
            queries.selectByClientID(clientID).executeAsOneOrNull()
        }
    }

    suspend fun getAllConnections(): List<MqttConnection> {
        return withContext(Dispatchers.IO) {
            queries.selectAll().executeAsList()
        }
    }

    fun getAllConnectionsFlow(): Flow<List<MqttConnection>> {
        return queries.selectAll().asFlow().mapToList(Dispatchers.IO)
    }

    suspend fun deleteConnectionByClientId(clientID: String) {
        withContext(Dispatchers.IO) {
            queries.deleteByClientId(clientID)
        }
    }

    suspend fun deleteAllConnections() {
        withContext(Dispatchers.IO) {
            queries.deleteAll()
        }
    }
}
