package `in`.eswarm.mahati.log

import `in`.eswarm.mahati.data.data.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LogStream(private val settingsDataStore: SettingsDataStore) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val logFlow: Flow<List<LogData>>
        get() {
            return settingsDataStore.logs.map {
                it.map { logString -> LogData(logString) }.sortedBy { it.msg }
            }
        }

    fun addLog(logData: LogData) {
        scope.launch {
            settingsDataStore.addLog(logData.msg)
        }
    }

    fun clear() {
        scope.launch {
            settingsDataStore.clearLogs()
        }
    }
}