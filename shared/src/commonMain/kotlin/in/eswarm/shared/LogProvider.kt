package `in`.eswarm.shared

import kotlinx.coroutines.flow.Flow

interface LogProvider {

    val logs: Flow<List<String>> // Changed from Set to List

    suspend fun addLog(msg: String)

    suspend fun clearLogs()

}
