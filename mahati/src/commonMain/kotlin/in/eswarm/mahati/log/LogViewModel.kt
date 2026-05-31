package `in`.eswarm.mahati.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import `in`.eswarm.shared.LogStream
import kotlin.reflect.KClass

class LogViewModel(val logStream: LogStream) : ViewModel() {
    fun clearLogs() {
        logStream.clear()
    }

    val logs = logStream.logFlow

    companion object {
        fun Factory(logStream: LogStream): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(
                    modelClass: KClass<T>,
                    extras: CreationExtras
                ): T {
                    return LogViewModel(logStream) as T
                }
            }
    }
}
