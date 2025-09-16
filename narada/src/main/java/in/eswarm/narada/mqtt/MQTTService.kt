package `in`.eswarm.narada.mqtt

import `in`.eswarm.narada.R
import `in`.eswarm.narada.launch.LaunchActivity
import `in`.eswarm.narada.log.LogStream
import `in`.eswarm.narada.util.NotificationUtil.FG_SERVICE_CHANNEL
import `in`.eswarm.narada.util.getAppComponent
import `in`.eswarm.narada.util.preferences
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.content.ContextCompat
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MQTTService : Service() {

    lateinit var threadExecutor: ExecutorService
    lateinit var logStream: LogStream

    override fun onCreate() {
        super.onCreate()
        logStream = applicationContext.getAppComponent().logStream
        threadExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == START) {
            init()
            return START_STICKY
        } else if (intent?.action == STOP) {
            stop()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    private fun stop() {
        isMoquetteRunning = false
        MQTTWrapper.stopMoquette()
        stopForeground(true)
        stopSelf()
    }

    private fun init() {
        threadExecutor.submit {
            val mqttListener = getAppComponent().mqttServerListener

            val serverProperties = runBlocking {
                application.preferences.getServerProperties()
            }

            isMoquetteRunning = true
            MQTTWrapper.startMoquette(mqttListener, logStream, serverProperties)
        }

        val pendingIntent: PendingIntent =
            Intent(this, LaunchActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    this,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

        val notification: Notification = NotificationCompat.Builder(this, FG_SERVICE_CHANNEL)
            .setContentTitle(getText(R.string.notification_mqtt_service_title))
            .setContentText(getText(R.string.notification_mqtt_service_content))
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.notification_mqtt_ticker))
            .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
            .build()

        // Notification ID cannot be 0.
        startForeground(NOT_SERVICE_ID, notification)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        MQTTWrapper.stopMoquette()
        threadExecutor.shutdownNow()
    }

    companion object {
        const val NOT_SERVICE_ID = 987
        const val CHANNEL_DEFAULT = "channel"
        const val START = "start"
        const val STOP = "stop"
        var isMoquetteRunning: Boolean = false

        fun start(context: Context) {
            val intent = Intent(context, MQTTService::class.java)
            intent.action = START
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MQTTService::class.java)
            intent.action = STOP
            ContextCompat.startForegroundService(context, intent)
        }
    }
}