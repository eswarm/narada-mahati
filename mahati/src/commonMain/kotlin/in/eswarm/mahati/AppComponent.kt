package `in`.eswarm.mahati

import `in`.eswarm.mahati.db.ConnectionAdapter
import `in`.eswarm.mahati.mqtt.core.HiveMqMqttManagerImpl
import `in`.eswarm.mahati.mqtt.core.MqttManager

class AppComponent {

    val mqttManager: MqttManager = HiveMqMqttManagerImpl()
    val connectionRepo: ConnectionAdapter = ConnectionAdapter()

    fun clear() {}
}
