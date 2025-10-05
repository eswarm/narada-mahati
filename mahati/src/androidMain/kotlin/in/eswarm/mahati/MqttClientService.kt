package `in`.eswarm.mahati

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import `in`.eswarm.mahati.db.MqttConnection
import `in`.eswarm.mahati.mqtt.controller.MqttConnectionController
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
        )

        // Basic foreground notification - customize as needed
        val notification =
            NotificationCompat.Builder(this, FG_SERVICE_CHANNEL) // Create this channel
                .setContentTitle("Mahati Mqtt client").setContentText("Mahati Mqtt client")
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app's icon
                .setPriority(NotificationCompat.PRIORITY_HIGH).build()
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
        // Handle specific actions from intents if needed, e.g., auto-connecting
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        multiConnectionController.shutdownAll()
        serviceJob.cancel() // Cancels serviceScope and all its children
    }

    override fun onBind(intent: Intent): IBinder = binder

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MqttClientService::class.java)
            ContextCompat.startForegroundService(context, intent) // If API 26+
        }

        fun stop(context: Context) {
            val intent = Intent(context, MqttClientService::class.java)
            context.stopService(intent)
        }
    }
}
