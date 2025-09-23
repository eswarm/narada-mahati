package `in`.eswarm.mahati

import `in`.eswarm.mahati.db.ConnectionAdapter
import `in`.eswarm.mahati.db.MessageRepository
import `in`.eswarm.mahati.db.SubscriptionRepository
import `in`.eswarm.mahati.mqtt.core.HiveMqMqttManagerImpl
import `in`.eswarm.mahati.mqtt.core.MqttManager

class AppComponent {

    val mqttManager: MqttManager = HiveMqMqttManagerImpl()
    val connectionRepo: ConnectionAdapter = ConnectionAdapter()

    val subscriptionRepo: SubscriptionRepository = SubscriptionRepository()

    val messageRepo: MessageRepository = MessageRepository()
    fun clear() {}
}
