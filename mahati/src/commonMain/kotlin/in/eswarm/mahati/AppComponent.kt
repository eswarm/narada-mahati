package `in`.eswarm.mahati

import `in`.eswarm.mahati.data.data.SettingsDataStore
import `in`.eswarm.mahati.db.ConnectionAdapter
import `in`.eswarm.mahati.db.MessageRepository
import `in`.eswarm.mahati.db.SubscriptionRepository
import `in`.eswarm.mahati.log.LogStream
import `in`.eswarm.mahati.log.MahatiLoggerFactory
import `in`.eswarm.mahati.mqtt.di.getMqttController
import `in`.eswarm.mahati.mqtt.service.MqttControllerContract
import org.slf4j.impl.StaticLoggerBinder

class AppComponent(settingsDataStore: SettingsDataStore) {
    val mqttController: MqttControllerContract = getMqttController()
    val connectionRepo: ConnectionAdapter = ConnectionAdapter()
    val subscriptionRepo: SubscriptionRepository = SubscriptionRepository()
    val messageRepo: MessageRepository = MessageRepository()
    val logStream: LogStream = LogStream(settingsDataStore)

    init {
        val binder = StaticLoggerBinder.getSingleton()
        binder.init(MahatiLoggerFactory(logStream))
    }

    fun clear() {}
}
