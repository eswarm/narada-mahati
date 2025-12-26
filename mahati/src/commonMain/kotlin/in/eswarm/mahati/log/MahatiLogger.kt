package `in`.eswarm.mahati.log

import `in`.eswarm.shared.LogData
import `in`.eswarm.shared.LogStream
import org.slf4j.Logger
import org.slf4j.Marker

class MahatiLogger(private val logStream: LogStream, private val name: String) : Logger {

    private fun log(level: String, msg: String) {
        logStream.addLog(LogData(tag = "HIVE_MQ", "[$name] $level: $msg"))
    }

    override fun getName(): String = name
    override fun isTraceEnabled(): Boolean = true
    override fun isTraceEnabled(marker: Marker?): Boolean = true
    override fun trace(msg: String?) {
        msg?.let { log("TRACE", it) }
    }

    override fun trace(format: String?, arg: Any?) {}
    override fun trace(format: String?, arg1: Any?, arg2: Any?) {}
    override fun trace(format: String?, vararg arguments: Any?) {}
    override fun trace(msg: String?, t: Throwable?) {
        msg?.let { log("TRACE", it) }
    }

    override fun trace(marker: Marker?, msg: String?) {}
    override fun trace(marker: Marker?, format: String?, arg: Any?) {}
    override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {}
    override fun trace(marker: Marker?, format: String?, vararg arguments: Any?) {}
    override fun trace(marker: Marker?, msg: String?, t: Throwable?) {}

    override fun isDebugEnabled(): Boolean = true
    override fun isDebugEnabled(marker: Marker?): Boolean = true
    override fun debug(msg: String?) {
        msg?.let { log("DEBUG", it) }
    }

    override fun debug(format: String?, arg: Any?) {}
    override fun debug(format: String?, arg1: Any?, arg2: Any?) {}
    override fun debug(format: String?, vararg arguments: Any?) {}
    override fun debug(msg: String?, t: Throwable?) {
        msg?.let { log("DEBUG", it) }
    }

    override fun debug(marker: Marker?, msg: String?) {}
    override fun debug(marker: Marker?, format: String?, arg: Any?) {}
    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {}
    override fun debug(marker: Marker?, format: String?, vararg arguments: Any?) {}
    override fun debug(marker: Marker?, msg: String?, t: Throwable?) {}

    override fun isInfoEnabled(): Boolean = true
    override fun isInfoEnabled(marker: Marker?): Boolean = true
    override fun info(msg: String?) {
        msg?.let { log("INFO", it) }
    }

    override fun info(format: String?, arg: Any?) {}
    override fun info(format: String?, arg1: Any?, arg2: Any?) {}
    override fun info(format: String?, vararg arguments: Any?) {}
    override fun info(msg: String?, t: Throwable?) {
        msg?.let { log("INFO", it) }
    }

    override fun info(marker: Marker?, msg: String?) {}
    override fun info(marker: Marker?, format: String?, arg: Any?) {}
    override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {}
    override fun info(marker: Marker?, format: String?, vararg arguments: Any?) {}
    override fun info(marker: Marker?, msg: String?, t: Throwable?) {}

    override fun isWarnEnabled(): Boolean = true
    override fun isWarnEnabled(marker: Marker?): Boolean = true
    override fun warn(msg: String?) {
        msg?.let { log("WARN", it) }
    }

    override fun warn(format: String?, arg: Any?) {}
    override fun warn(format: String?, vararg arguments: Any?) {}
    override fun warn(format: String?, arg1: Any?, arg2: Any?) {}
    override fun warn(msg: String?, t: Throwable?) {
        msg?.let { log("WARN", it) }
    }

    override fun warn(marker: Marker?, msg: String?) {}
    override fun warn(marker: Marker?, format: String?, arg: Any?) {}
    override fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {}
    override fun warn(marker: Marker?, format: String?, vararg arguments: Any?) {}
    override fun warn(marker: Marker?, msg: String?, t: Throwable?) {}

    override fun isErrorEnabled(): Boolean = true
    override fun isErrorEnabled(marker: Marker?): Boolean = true
    override fun error(msg: String?) {
        msg?.let { log("ERROR", it) }
    }

    override fun error(format: String?, arg: Any?) {}
    override fun error(format: String?, arg1: Any?, arg2: Any?) {}
    override fun error(format: String?, vararg arguments: Any?) {}
    override fun error(msg: String?, t: Throwable?) {
        msg?.let { log("ERROR", it) }
    }

    override fun error(marker: Marker?, msg: String?) {}
    override fun error(marker: Marker?, format: String?, arg: Any?) {}
    override fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {}
    override fun error(marker: Marker?, format: String?, vararg arguments: Any?) {}
    override fun error(marker: Marker?, msg: String?, t: Throwable?) {}
}