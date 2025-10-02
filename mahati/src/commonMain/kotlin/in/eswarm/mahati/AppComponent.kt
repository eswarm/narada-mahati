package `in`.eswarm.mahati

import `in`.eswarm.mahati.db.ConnectionAdapter
import `in`.eswarm.mahati.db.MessageRepository
import `in`.eswarm.mahati.db.SubscriptionRepository
import `in`.eswarm.mahati.mqtt.di.getMqttController
import `in`.eswarm.mahati.mqtt.service.MqttControllerContract

class AppComponent {
    val mqttController: MqttControllerContract = getMqttController()
    val connectionRepo: ConnectionAdapter = ConnectionAdapter()
    val subscriptionRepo: SubscriptionRepository = SubscriptionRepository()

    val messageRepo: MessageRepository = MessageRepository()
    fun clear() {}
}
