package `in`.eswarm.narada.log

import kotlinx.coroutines.runBlocking

import org.junit.After
import org.junit.Before
import org.junit.Test

class LogStreamTest {

    lateinit var logStream: LogStream

    @Before
    fun setUp() {
        logStream = LogStream()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun test() {
        runBlocking {
            for (i in 1..1000) {
                logStream.addLog(LogData(i.toString()))
            }

            val logList = logStream.getAllLogs()

            assert(logList.size == 1000)
        }
    }
}