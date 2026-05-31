package org.slf4j.impl

import `in`.eswarm.narada.log.NaradaLoggerFactory
import org.slf4j.ILoggerFactory
import org.slf4j.spi.LoggerFactoryBinder

/**
 * This is the binding for SLF4J. It is loaded by SLF4J at runtime.
 * The package and class name are REQUIRED to be org.slf4j.impl.StaticLoggerBinder.
 */
class StaticLoggerBinder : LoggerFactoryBinder {

    companion object {
        // The SINGLETON field is REQUIRED by the SLF4J API
        @JvmStatic
        private val SINGLETON = StaticLoggerBinder()

        // The getSingleton() method is REQUIRED by the SLF4J API
        @JvmStatic
        fun getSingleton(): StaticLoggerBinder {
            return SINGLETON
        }
    }

    private var loggerFactory: ILoggerFactory? = null

    fun init(loggerFactory: ILoggerFactory) {
        this.loggerFactory = loggerFactory
    }

    override fun getLoggerFactory(): ILoggerFactory {
        // This check is to ensure that our AppComponent has initialized the logger.
        if (loggerFactory == null) {
            throw IllegalStateException(
                "NaradaLoggerFactory has not been initialized. \n" +
                        "Please ensure StaticLoggerBinder.getSingleton().init() is called in your AppComponent."
            )
        }
        return loggerFactory!!
    }

    override fun getLoggerFactoryClassStr(): String {
        return NaradaLoggerFactory::class.java.name
    }
}