package `in`.eswarm.mahati.db // Assuming this is where you want your repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class MqttConnectionModel(
    val id: Long,
    val brokerHost: String,
    val brokerPort: Long,
    val clientID: String,
    val username: String?,
    val password: ByteArray?,
    val useSsl: Boolean,
    val topicPrefix: String,
    val useWebsocket: Boolean = false,
    val webSocketPath: String = "",
    val createdAt: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MqttConnectionModel

        if (id != other.id) return false
        if (brokerPort != other.brokerPort) return false
        if (useSsl != other.useSsl) return false
        if (createdAt != other.createdAt) return false
        if (brokerHost != other.brokerHost) return false
        if (clientID != other.clientID) return false
        if (username != other.username) return false
        if (!password.contentEquals(other.password)) return false
        if (topicPrefix != other.topicPrefix) return false
        if (useWebsocket != other.useWebsocket) return false
        if (webSocketPath != other.webSocketPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + brokerPort.hashCode()
        result = 31 * result + useSsl.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + brokerHost.hashCode()
        result = 31 * result + clientID.hashCode()
        result = 31 * result + (username?.hashCode() ?: 0)
        result = 31 * result + (password?.contentHashCode() ?: 0)
        result = 31 * result + topicPrefix.hashCode()
        result = 31 * result + useWebsocket.hashCode()
        result = 31 * result + webSocketPath.hashCode()
        return result
    }
}

class ConnectionAdapter {

    private val queries = getMahatiDb().mqttConnectionParamsQueries

    suspend fun addConnection(
        brokerHost: String,
        brokerPort: Long,
        clientID: String,
        username: String?,
        password: ByteArray?,
        useSsl: Boolean,
        topicPrefix: String,
        useWebsocket: Boolean,
        webSocketPath: String
    ) {
        withContext(Dispatchers.IO) { // Perform DB operation on IO dispatcher
            queries.insert(
                brokerHost = brokerHost,
                brokerPort = brokerPort,
                clientID = clientID,
                username = username,
                password = password,
                useSsl = useSsl,
                topicPrefix = topicPrefix,
                useWebsocket = useWebsocket,
                websocketPath = webSocketPath
            )
        }
    }

    suspend fun getConnectionByClientId(clientID: String): MqttConnectionModel? {
        return withContext(Dispatchers.IO) {
            val mqttConnection = queries.selectByClientID(clientID).executeAsOneOrNull()
            if (mqttConnection != null) {
                return@withContext MqttConnectionModel(
                    mqttConnection.id,
                    mqttConnection.brokerHost,
                    mqttConnection.brokerPort,
                    mqttConnection.clientID,
                    mqttConnection.username,
                    mqttConnection.password,
                    mqttConnection.useSsl,
                    mqttConnection.topicPrefix,
                    mqttConnection.useWebsocket,
                    mqttConnection.websocketPath,
                    mqttConnection.createdAt
                )
            }
            return@withContext null
        }
    }

    suspend fun getAllConnections(): List<MqttConnectionModel> {
        return withContext(Dispatchers.IO) {
            queries.selectAll(mapper = { id, brokerHost, brokerPort, clientID, username, password, useSsl, topicPrefix, useWebsocket, webSocketPath, createdAt ->
                MqttConnectionModel(
                    id,
                    brokerHost,
                    brokerPort,
                    clientID,
                    username,
                    password,
                    useSsl,
                    topicPrefix,
                    useWebsocket,
                    webSocketPath,
                    createdAt
                )
            }).executeAsList()
        }
    }

    fun getAllConnectionsFlow(): Flow<List<MqttConnectionModel>> {
        return queries.selectAll(
            mapper = { id, brokerHost, brokerPort, clientID, username, password, useSsl, topicPrefix, useWebsocket, webSocketPath, createdAt ->
                MqttConnectionModel(
                    id,
                    brokerHost,
                    brokerPort,
                    clientID,
                    username,
                    password,
                    useSsl,
                    topicPrefix,
                    useWebsocket,
                    webSocketPath,
                    createdAt
                )
            }).asFlow().mapToList(Dispatchers.IO)
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
