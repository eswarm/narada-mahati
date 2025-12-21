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
                    val parts = logString.split("-", limit = 2)
                    if (parts.size == 2) {
                        val timestamp = parts[0].toLongOrNull()
                        val message = parts[1]
                        if (timestamp != null) {
                            LogData(message, timestamp = timestamp)
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
            // Pass the formatted string to be stored by AppPreferences
            appPreferences.addLog("${logData.timestamp}-${logData.msg}")
        }
    }

    fun clear() {
        scope.launch {
            appPreferences.clearLogs()
        }
    }
}
