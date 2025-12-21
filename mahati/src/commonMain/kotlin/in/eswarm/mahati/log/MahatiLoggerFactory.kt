package `in`.eswarm.mahati.log

import org.slf4j.ILoggerFactory
import org.slf4j.Logger

/**
 * A factory that produces MahatiLogger instances.
 */
class MahatiLoggerFactory(private val logStream: LogStream) : ILoggerFactory {
    override fun getLogger(name: String): Logger {
        return MahatiLogger(logStream, name)
    }
}