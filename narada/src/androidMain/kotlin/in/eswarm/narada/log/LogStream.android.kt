package `in`.eswarm.narada.log

import android.util.Log

actual fun console(level: LogLevel, tag: String, msg: String) {
    when (level) {
        LogLevel.VERBOSE -> Log.v(tag, msg)
        LogLevel.INFO -> Log.i(tag, msg)
        LogLevel.DEBUG -> Log.d(tag, msg)
        LogLevel.WARN -> Log.w(tag, msg)
        LogLevel.ERROR -> Log.e(tag, msg)
    }
}