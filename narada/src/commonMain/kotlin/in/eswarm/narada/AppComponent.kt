package `in`.eswarm.narada

import `in`.eswarm.narada.log.LogStream
import `in`.eswarm.narada.log.NaradaLoggerFactory
import `in`.eswarm.narada.mqtt.MQTTServerListener
import `in`.eswarm.narada.preferences.AppPreferences
import org.slf4j.impl.StaticLoggerBinder

class AppComponent(val appPreferences: AppPreferences) {

    val logStream: LogStream = LogStream(appPreferences)
    val mqttServerListener: MQTTServerListener = MQTTServerListener(logStream)

    init {
        val binder = StaticLoggerBinder.getSingleton()
        binder.init(NaradaLoggerFactory(logStream))
    }

    fun clear() {
        logStream.clear()
    }

    companion object {
        lateinit var INSTANCE: AppComponent
    }
}
