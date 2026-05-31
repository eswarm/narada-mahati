package `in`.eswarm.mahati

import `in`.eswarm.mahati.data.data.SettingsDataStore
import `in`.eswarm.mahati.db.ConnectionAdapter
import `in`.eswarm.mahati.db.LogRepository
import `in`.eswarm.mahati.db.MessageRepository
import `in`.eswarm.mahati.db.SubscriptionRepository
import `in`.eswarm.mahati.db.getMahatiDb
import `in`.eswarm.mahati.log.mahatiLogger
import `in`.eswarm.mahati.mqtt.controller.MqttConnectionController
import `in`.eswarm.shared.LogStream
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppComponent(
    sendNotification: ((String, String, String, String) -> Unit)? = null,
    private val onConnectionAction: (() -> Unit)? = null
) {
    private val appScope = CoroutineScope(SupervisorJob() + CoroutineName("MahatiAppScope"))

    val connectionRepo: ConnectionAdapter = ConnectionAdapter()
    val subscriptionRepo: SubscriptionRepository = SubscriptionRepository()
    val messageRepo: MessageRepository = MessageRepository()
    private val logRepository: LogRepository = LogRepository(getMahatiDb())
    val logStream: LogStream = LogStream(logRepository)
    val settingsDataStore: SettingsDataStore = SettingsDataStore(getMahatiDb())

    val mqttController: MqttConnectionController

    init {
        // 1. Set the logStream for the logger.
        // SLF4J will now automatically initialize the factory via MahatiServiceProvider.
        mahatiLogger.logStream = logStream

        // 2. Create the MQTT controller.
        mqttController = MqttConnectionController(
            controllerScope = appScope,
            messageRepo = messageRepo,
            subscriptionRepo = subscriptionRepo,
            sendNotification = sendNotification,
            onConnectionAdded = onConnectionAction,
            settingsDataStore = settingsDataStore
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
