package `in`.eswarm.narada

import `in`.eswarm.narada.log.LogStream
import `in`.eswarm.narada.mqtt.MQTTServerListener

class AppComponent {

    val logStream: LogStream
    val mqttServerListener: MQTTServerListener

    init {
        logStream = LogStream()
        mqttServerListener = MQTTServerListener(logStream)
    }

    fun clear() {
        logStream.clear()
    }
}
