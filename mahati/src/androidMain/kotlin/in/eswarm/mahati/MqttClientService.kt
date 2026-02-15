package `in`.eswarm.mahati

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import `in`.eswarm.mahati.home.HomeActivity
import `in`.eswarm.mahati.mqtt.service.MqttControllerContract
import `in`.eswarm.mahati.util.NotificationUtil.FG_SERVICE_CHANNEL
import `in`.eswarm.mahati.util.getAppComponent

class MqttClientService : Service() {

    private lateinit var mqttController: MqttControllerContract

    override fun onCreate() {
        super.onCreate()

        val appComponent = getAppComponent()
        mqttController = appComponent.mqttController


        // Create the intent that will be sent when the user clicks the "Stop" button
        val stopSelfIntent = Intent(this, MqttClientService::class.java).apply {
            action = ACTION_STOP
        }
        val stopSelfPendingIntent: PendingIntent = PendingIntent.getService(
            this, 0, stopSelfIntent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to open the app when clicking the notification
        val contentIntent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 1, contentIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the foreground notification with the stop action
        val notification =
            NotificationCompat.Builder(this, FG_SERVICE_CHANNEL)
                .setContentTitle("Mahati Mqtt client")
                .setContentText("Mahati Mqtt client is running")
                .setSmallIcon(R.drawable.notif_icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentPendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopSelfPendingIntent)
                .build()

        startForeground(101, notification) // Unique notification ID
    }

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

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    companion object {
        const val ACTION_STOP = "in.eswarm.mahati.service.action.STOP"
    }
}
