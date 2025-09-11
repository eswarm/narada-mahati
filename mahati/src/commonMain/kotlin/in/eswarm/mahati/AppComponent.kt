package `in`.eswarm.mahati

import `in`.eswarm.mahati.db.ConnectionRepository
import `in`.eswarm.mahati.mqtt.core.HiveMqMqttManagerImpl
import `in`.eswarm.mahati.mqtt.core.MqttManager

class AppComponent {

    val mqttManager: MqttManager = HiveMqMqttManagerImpl()
    val connRepo: ConnectionRepository = ConnectionRepository()

    fun clear() {
    }
}
