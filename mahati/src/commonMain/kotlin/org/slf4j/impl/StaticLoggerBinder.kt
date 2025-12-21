package org.slf4j.impl

import `in`.eswarm.mahati.log.MahatiLoggerFactory
import org.slf4j.ILoggerFactory
import org.slf4j.spi.LoggerFactoryBinder

class StaticLoggerBinder : LoggerFactoryBinder {

    companion object {
        @JvmStatic
        private val SINGLETON = StaticLoggerBinder()

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
        if (loggerFactory == null) {
            throw IllegalStateException(
                "MahatiLoggerFactory has not been initialized. \n" +
                        "Please ensure StaticLoggerBinder.getSingleton().init() is called in your AppComponent."
            )
        }
        return loggerFactory!!
    }

    override fun getLoggerFactoryClassStr(): String {
        return MahatiLoggerFactory::class.java.name
    }
}