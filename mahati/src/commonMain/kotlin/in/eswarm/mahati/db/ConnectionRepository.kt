package `in`.eswarm.mahati.db // Assuming this is where you want your repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ConnectionRepository {

    private val queries = getMahatiDb().mqttConnectionParamsQueries

    suspend fun addConnection( // Added suspend modifier
        brokerHost: String,
        brokerPort: Long,
        clientId: String,
        username: String?,
        password: ByteArray?,
        useSsl: Boolean,
        topicPrefix: String,
        version: Long
    ) {
        withContext(Dispatchers.IO) { // Perform DB operation on IO dispatcher
            queries.insert(
                brokerHost = brokerHost,
                brokerPort = brokerPort,
                clientId = clientId,
                username = username,
                password = password,
                useSsl = if (useSsl) 1 else 0, // Corrected: Pass Boolean directly, adapter handles it
                topicPrefix = topicPrefix,
                version = version
            )
        }
    }

    suspend fun getConnectionByClientId(clientId: String): MqttConnectionParamsEntity? {
        return withContext(Dispatchers.IO) {
            queries.selectByClientId(clientId).executeAsOneOrNull()
        }
    }

    fun getConnectionByClientIdFlow(clientId: String): Flow<MqttConnectionParamsEntity?> {
        return queries.selectByClientId(clientId).asFlow().mapToOneOrNull(Dispatchers.IO)
    }

    suspend fun getAllConnections(): List<MqttConnectionParamsEntity> {
        return withContext(Dispatchers.IO) {
            queries.selectAll().executeAsList()
        }
    }

    fun getAllConnectionsFlow(): Flow<List<MqttConnectionParamsEntity>> {
        return queries.selectAll().asFlow().mapToList(Dispatchers.IO)
    }

    suspend fun deleteConnectionByClientId(clientId: String) {
        withContext(Dispatchers.IO) {
            queries.deleteByClientId(clientId)
        }
    }

    suspend fun deleteAllConnections() {
        withContext(Dispatchers.IO) {
            queries.deleteAll()
        }
    }
}
