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
            return appPreferences.logs.map {
                it.map { logString -> LogData(logString) }.sortedBy { it.msg }
            }
        }

    fun addLog(logData: LogData) {
        scope.launch {
            appPreferences.addLog(logData.msg)
        }
    }

    fun clear() {
        scope.launch {
            appPreferences.clearLogs()
        }
    }
}