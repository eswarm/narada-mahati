package `in`.eswarm.narada.log

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class LogStream {

    private val _logFlow = MutableSharedFlow<LogData>(
        extraBufferCapacity = 1000,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val logFlow: SharedFlow<LogData>
        get() {
            return _logFlow
        }

    fun addLog(logData: LogData) {
        scope.launch {
            _logFlow.emit(logData)
        }
    }

    fun getAllLogs(): List<LogData> {
        return logFlow.replayCache
    }

    fun clear() {
        scope.coroutineContext.cancelChildren()
    }
}