package `in`.eswarm.narada.log

/**
 * Represents a single log entry with a message and a timestamp.
 */
data class LogData(
    val msg: String,
    val level: MsgLevel = MsgLevel.INFO,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MsgLevel {
    VERBOSE, INFO, DEBUG, WARN, ERROR
}