package `in`.eswarm.mahati.log

import org.slf4j.ILoggerFactory
import org.slf4j.Logger

val mahatiLogger = MahatiLogger("Mahati")

/**
 * A factory that produces MahatiLogger instances.
 */
class MahatiLoggerFactory() : ILoggerFactory {
    override fun getLogger(name: String): Logger {
        return mahatiLogger
    }
}