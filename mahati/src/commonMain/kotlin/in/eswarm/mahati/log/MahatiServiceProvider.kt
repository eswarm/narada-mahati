package `in`.eswarm.mahati.log

import org.slf4j.ILoggerFactory
import org.slf4j.IMarkerFactory
import org.slf4j.helpers.BasicMarkerFactory
import org.slf4j.helpers.NOPMDCAdapter
import org.slf4j.spi.MDCAdapter
import org.slf4j.spi.SLF4JServiceProvider

class MahatiServiceProvider : SLF4JServiceProvider {

    private val loggerFactory: ILoggerFactory = MahatiLoggerFactory()
    private val markerFactory = BasicMarkerFactory()
    private val mdcAdapter: MDCAdapter = NOPMDCAdapter()

    override fun getLoggerFactory(): ILoggerFactory = loggerFactory

    override fun getMarkerFactory(): IMarkerFactory = markerFactory

    override fun getMDCAdapter(): MDCAdapter = mdcAdapter

    override fun getRequestedApiVersion(): String = "2.0.99"

    override fun initialize() {
    }
}
