package `in`.eswarm.narada

import `in`.eswarm.narada.log.LogStream
import `in`.eswarm.narada.mqtt.MQTTServerListener
import `in`.eswarm.narada.preferences.AppPreferences
import `in`.eswarm.narada.util.Logger

class AppComponent(val appPreferences: AppPreferences) {

    val logStream: LogStream = LogStream(appPreferences)
    val mqttServerListener: MQTTServerListener = MQTTServerListener(logStream)

    fun clear() {
        logStream.clear()
    }

    companion object {
        lateinit var INSTANCE: AppComponent
    }
}
