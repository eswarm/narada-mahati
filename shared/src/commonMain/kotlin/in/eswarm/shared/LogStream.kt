package `in`.eswarm.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LogStream(private val logProvider: LogProvider) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val logFlow: Flow<List<LogData>>
        get() {
            return logProvider.logs.map { logSet ->
                logSet.mapNotNull { logString ->
                    // Safely parse the string, splitting it into timestamp and message
                    val parts = logString.split("-", limit = 4)
                    if (parts.size == 4) {
                        val timestamp = parts[0].toLongOrNull()
                        val level = LogLevel.fromString(parts[1]) ?: LogLevel.INFO
                        val tag = parts[2]
                        val message = parts[3]
                        if (timestamp != null) {
                            LogData(msg = message, level = level, tag = tag, timestamp = timestamp)
                        } else {
                            null // Or handle legacy logs without timestamps
                        }
                    } else {
                        null
                    }
                }.sortedBy { it.timestamp } // Sort by the parsed timestamp
            }
        }

    fun addLog(logData: LogData) {
        scope.launch {
            console(logData.level, logData.tag, logData.msg)
            logProvider.addLog("${logData.timestamp}-${logData.level}-${logData.tag}-${logData.msg}")
        }
    }

    fun clear() {
        scope.launch {
            logProvider.clearLogs()
        }
    }
}

expect fun console(level: LogLevel, tag: String, msg: String)
