package `in`.eswarm.narada.log

import `in`.eswarm.narada.preferences.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LogStream(private val appPreferences: AppPreferences) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val logFlow: Flow<List<LogData>>
        get() {
            return appPreferences.logs.map { logSet ->
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
            appPreferences.addLog("${logData.timestamp}-${logData.level}-${logData.tag}-${logData.msg}")
        }
    }

    fun clear() {
        scope.launch {
            appPreferences.clearLogs()
        }
    }
}

expect fun console(level: LogLevel, tag: String, msg: String)
