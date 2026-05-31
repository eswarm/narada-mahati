package `in`.eswarm.narada

import `in`.eswarm.narada.log.NaradaLoggerFactory
import `in`.eswarm.narada.mqtt.MQTTWrapper
import `in`.eswarm.narada.preferences.AppPreferences
import `in`.eswarm.narada.service.ServerManager
import `in`.eswarm.narada.service.getServerManager
import `in`.eswarm.shared.LogStream
import org.slf4j.impl.StaticLoggerBinder

class AppComponent(val appPreferences: AppPreferences) {

    val logStream: LogStream = LogStream(appPreferences)
    val mqttWrapper: MQTTWrapper = MQTTWrapper( logStream)
    val serverManager: ServerManager = getServerManager(mqttWrapper, appPreferences)

    init {
        val binder = StaticLoggerBinder.getSingleton()
        binder.init(NaradaLoggerFactory(logStream))
    }

    fun clear() {
        logStream.clear()
    }
}
