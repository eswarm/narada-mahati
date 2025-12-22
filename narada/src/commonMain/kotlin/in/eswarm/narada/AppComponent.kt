package `in`.eswarm.narada

import `in`.eswarm.narada.log.LogStream
import `in`.eswarm.narada.log.NaradaLoggerFactory
import `in`.eswarm.narada.mqtt.MQTTServerListener
import `in`.eswarm.narada.mqtt.MQTTWrapper
import `in`.eswarm.narada.preferences.AppPreferences
import `in`.eswarm.narada.service.ServerManager
import `in`.eswarm.narada.service.getServerManager
import org.slf4j.impl.StaticLoggerBinder

class AppComponent(val appPreferences: AppPreferences) {

    val logStream: LogStream = LogStream(appPreferences)
    val mqttServerListener: MQTTServerListener = MQTTServerListener(logStream)
    val mqttWrapper: MQTTWrapper = MQTTWrapper(mqttServerListener, logStream)
    val serverManager: ServerManager = getServerManager()

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
