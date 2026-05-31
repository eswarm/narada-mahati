package `in`.eswarm.narada.log

import `in`.eswarm.shared.LogStream
import org.slf4j.ILoggerFactory
import org.slf4j.Logger

/**
 * A factory that produces NaradaLogger instances.
 */
class NaradaLoggerFactory(private val logStream: LogStream) : ILoggerFactory {
    override fun getLogger(name: String): Logger {
        return NaradaLogger(logStream, name)
    }
}