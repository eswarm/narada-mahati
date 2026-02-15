package `in`.eswarm.mahati

import `in`.eswarm.mahati.data.data.SettingsDataStore
import `in`.eswarm.mahati.db.ConnectionAdapter
import `in`.eswarm.mahati.db.MessageRepository
import `in`.eswarm.mahati.db.SubscriptionRepository
import `in`.eswarm.mahati.log.MahatiLoggerFactory
import `in`.eswarm.mahati.log.mahatiLogger
import `in`.eswarm.mahati.mqtt.controller.MqttConnectionController
import `in`.eswarm.shared.LogStream
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.impl.StaticLoggerBinder

class AppComponent(
    settingsDataStore: SettingsDataStore,
    sendNotification: ((String, String, String, String) -> Unit)? = null,
    private val onConnectionAction: (() -> Unit)? = null
) {
    private val appScope = CoroutineScope(SupervisorJob() + CoroutineName("MahatiAppScope"))

    val connectionRepo: ConnectionAdapter = ConnectionAdapter()
    val subscriptionRepo: SubscriptionRepository = SubscriptionRepository()
    val messageRepo: MessageRepository = MessageRepository()
    val logStream: LogStream = LogStream(settingsDataStore)

    val mqttController: MqttConnectionController

    init {
        // 1. Initialize the logging system FIRST.
        val binder = StaticLoggerBinder.getSingleton()
        binder.init(MahatiLoggerFactory())
        mahatiLogger.logStream = logStream

        // 2. NOW, create the MQTT controller.
        mqttController = MqttConnectionController(
            controllerScope = appScope,
            messageRepo = messageRepo,
            subscriptionRepo = subscriptionRepo,
            sendNotification = sendNotification,
            onConnectionAdded = onConnectionAction
        )

        // Load all saved connections from the database and add them to the controller.
        appScope.launch {
            val connections = connectionRepo.getAllConnections()
            if (connections.isNotEmpty()) {
                onConnectionAction?.invoke()
            }
            for (connection in connections) {
                mqttController.addConnection(connection)
            }
        }
    }

    fun clear() {
        mqttController.shutdownAll()
    }
}
