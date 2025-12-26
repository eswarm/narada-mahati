package `in`.eswarm.mahati

import `in`.eswarm.mahati.data.data.SettingsDataStore
import `in`.eswarm.mahati.db.ConnectionAdapter
import `in`.eswarm.mahati.db.MessageRepository
import `in`.eswarm.mahati.db.SubscriptionRepository
import `in`.eswarm.mahati.log.MahatiLoggerFactory
import `in`.eswarm.mahati.mqtt.di.getMqttController
import `in`.eswarm.mahati.mqtt.service.MqttControllerContract
import `in`.eswarm.shared.LogStream
import org.slf4j.impl.StaticLoggerBinder

class AppComponent(settingsDataStore: SettingsDataStore) {
    val connectionRepo: ConnectionAdapter = ConnectionAdapter()
    val subscriptionRepo: SubscriptionRepository = SubscriptionRepository()
    val messageRepo: MessageRepository = MessageRepository()
    val logStream: LogStream = LogStream(settingsDataStore)

    lateinit var mqttController: MqttControllerContract

    init {
        // 1. Initialize the logging system FIRST.
        val binder = StaticLoggerBinder.getSingleton()
        binder.init(MahatiLoggerFactory(logStream))

        // 2. NOW, create the MQTT controller. When getMqttController() is called,
        //    SLF4J will find our fully initialized logger.
        mqttController = getMqttController()
    }

    fun clear() {}
}
