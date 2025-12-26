package `in`.eswarm.shared

import kotlinx.coroutines.flow.Flow

interface LogProvider {

    val logs: Flow<Set<String>>

    suspend fun addLog(msg: String)

    suspend fun clearLogs()

}