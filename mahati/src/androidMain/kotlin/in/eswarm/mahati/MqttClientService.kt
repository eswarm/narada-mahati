package `in`.eswarm.mahati

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import `in`.eswarm.mahati.db.MqttConnection
import `in`.eswarm.mahati.mqtt.controller.MqttConnectionController
import `in`.eswarm.mahati.util.NotificationUtil
import `in`.eswarm.mahati.util.NotificationUtil.FG_SERVICE_CHANNEL
import `in`.eswarm.mahati.util.getAppComponent
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MqttClientService : Service() {
    private val binder = LocalBinder()
    private val serviceJob = SupervisorJob()
    private val serviceScope =
        CoroutineScope(Dispatchers.IO + serviceJob + CoroutineName("AndroidMqttServiceScope"))

    lateinit var multiConnectionController: MqttConnectionController
        private set

    inner class LocalBinder : Binder() {
        fun getService(): MqttClientService = this@MqttClientService
    }

    override fun onCreate() {
        super.onCreate()
        multiConnectionController = MqttConnectionController(
            serviceScope,
            this.applicationContext.getAppComponent().messageRepo
        ) { title, message, clientID, topicName ->
            NotificationUtil.sendNotification(this, title, message, clientID, topicName)
        }

        // Create the intent that will be sent when the user clicks the "Stop" button
        val stopSelfIntent = Intent(this, MqttClientService::class.java).apply {
            action = ACTION_STOP
        }
        val stopSelfPendingIntent: PendingIntent = PendingIntent.getService(
            this, 0, stopSelfIntent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the foreground notification with the stop action
        val notification =
            NotificationCompat.Builder(this, FG_SERVICE_CHANNEL) // Create this channel
                .setContentTitle("Mahati Mqtt client")
                .setContentText("Mahati Mqtt client is running")
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app's icon
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopSelfPendingIntent)
                .build()

        startForeground(101, notification) // Unique notification ID
    }

    // --- Public API for bound clients (e.g., ViewModels) ---
    fun addConnection(config: MqttConnection) = multiConnectionController.addConnection(config)
    fun removeConnection(connectionId: String) =
        multiConnectionController.removeConnection(connectionId)

    suspend fun publish(
        connectionId: String, topic: String, payload: ByteArray, qos: Int, retain: Boolean
    ): Boolean {
        return multiConnectionController.publish(connectionId, topic, payload, qos, retain)
    }

    suspend fun subscribe(connectionId: String, topicFilter: String, qos: Int): Boolean {
        return multiConnectionController.subscribe(connectionId, topicFilter, qos)
    }

    suspend fun unsubscribe(connectionId: String, topicFilter: String): Boolean {
        return multiConnectionController.unsubscribe(connectionId, topicFilter)
    }

    // --- End Public API ---

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle the stop action from the notification
        if (intent?.action == ACTION_STOP) {
            // Calling stopSelf() will trigger onDestroy(), which handles the cleanup.
            stopSelf()
            return START_NOT_STICKY // Do not restart the service automatically.
        }
        // Handle other specific actions from intents if needed, e.g., auto-connecting
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        multiConnectionController.shutdownAll()
        serviceJob.cancel() // Cancels serviceScope and all its children
    }

    override fun onBind(intent: Intent): IBinder = binder

    companion object {
        const val ACTION_STOP = "in.eswarm.mahati.service.action.STOP"
    }
}
