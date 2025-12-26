package `in`.eswarm.narada.log

/**
 * Represents a single log entry with a message and a timestamp.
 */
data class LogData(
    val tag: String,
    val msg: String,
    val level: LogLevel = LogLevel.INFO,
    val timestamp: Long = System.currentTimeMillis()
)

enum class LogLevel {
    VERBOSE, INFO, DEBUG, WARN, ERROR;

    companion object {
        fun fromString(level: String): LogLevel? {
            LogLevel.entries.forEach {
                if (it.name.equals(level, ignoreCase = true)) {
                    return it
                }
            }
            return null
        }

    }
}