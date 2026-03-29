package `in`.eswarm.mahati

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import `in`.eswarm.mahati.home.HomeActivity
import `in`.eswarm.mahati.mqtt.service.MqttControllerContract
import `in`.eswarm.mahati.util.BatteryOptimizationHelper
import `in`.eswarm.mahati.util.NotificationUtil.FG_SERVICE_CHANNEL
import `in`.eswarm.mahati.util.getAppComponent

class MqttClientService : Service() {

    private lateinit var mqttController: MqttControllerContract
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()

        val appComponent = getAppComponent()
        mqttController = appComponent.mqttController

        // Request battery optimization exemption for MQTT keepalive during Doze mode
        requestBatteryOptimizationExemption()

        // Acquire wake lock to keep network alive for MQTT keepalive during Doze mode
        acquireWakeLock()

        // ...existing code...
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
            // Shut down all connections before stopping the service
            mqttController.removeAllConnections()
            mqttController.shutdownAll()
            stopSelf()
            return START_NOT_STICKY // Do not restart the service automatically.
        }
        // Handle other specific actions from intents if needed, e.g., auto-connecting
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release wake lock before shutting down
        releaseWakeLock()
        // Ensure that connections are shut down when the service is destroyed.
        mqttController.shutdownAll()
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "Mahati::MqttKeepaliveWakeLock"
            )
            wakeLock?.acquire(10 * 60 * 60 * 1000L) // 10 hours max, will be released in onDestroy
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!BatteryOptimizationHelper.isIgnoringBatteryOptimizations(this)) {
                try {
                    BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(this)
                } catch (e: Exception) {
                    // Permission not granted or other issue, continue without exemption
                    // User can manually disable battery optimization in settings
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    companion object {
        const val ACTION_STOP = "in.eswarm.mahati.service.action.STOP"
    }
}
