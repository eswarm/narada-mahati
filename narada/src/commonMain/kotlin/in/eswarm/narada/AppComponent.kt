package `in`.eswarm.narada

import `in`.eswarm.narada.log.LogStream
import `in`.eswarm.narada.mqtt.MQTTServerListener
import `in`.eswarm.narada.preferences.AppPreferences

class AppComponent(val appPreferences: AppPreferences) {

    val logStream: LogStream
    val mqttServerListener: MQTTServerListener

    init {
        logStream = LogStream()
        mqttServerListener = MQTTServerListener(logStream)
    }

    fun clear() {
        logStream.clear()
    }

    companion object {
        lateinit var INSTANCE: AppComponent
    }
}
