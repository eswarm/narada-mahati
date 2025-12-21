package `in`.eswarm.mahati.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LogViewModel(logStream: LogStream) : ViewModel() {

    val logs = logStream.logFlow

    companion object {
        fun Factory(logStream: LogStream): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LogViewModel(logStream) as T
            }
        }
    }
}
