package `in`.eswarm.narada.service

import android.app.Notification
import android.app.Notification.FOREGROUND_SERVICE_IMMEDIATE
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import `in`.eswarm.narada.AppComponent
import `in`.eswarm.narada.R
import `in`.eswarm.narada.launch.LaunchActivity
import `in`.eswarm.narada.mqtt.MQTTWrapper
import `in`.eswarm.narada.util.NotificationUtil.FG_SERVICE_CHANNEL
import `in`.eswarm.narada.util.preferences
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MQTTServerService : Service() {

    lateinit var threadExecutor: ExecutorService

    override fun onCreate() {
        super.onCreate()
        threadExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == START) {
            init()
            return START_STICKY
        } else if (intent?.action == STOP) {
            stopService()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun stopService() {
        MQTTWrapper.stopMoquette()
        stopForeground(true)
        stopSelf()
    }

    private fun init() {
        threadExecutor.submit {
            val serverProperties = runBlocking {
                application.preferences.getServerProperties()
            }
            MQTTWrapper.startMoquette(
                AppComponent.INSTANCE.mqttServerListener,
                AppComponent.INSTANCE.logStream,
                serverProperties
            )
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

        startForeground(NOT_SERVICE_ID, notification)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        threadExecutor.shutdownNow()
    }

    companion object {
        const val NOT_SERVICE_ID = 987
        const val START = "start"
        const val STOP = "stop"

        fun start(context: Context) {
            val intent = Intent(context, MQTTServerService::class.java)
            intent.action = MQTTServerService.START
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MQTTServerService::class.java)
            intent.action = MQTTServerService.STOP
            ContextCompat.startForegroundService(context, intent)
        }

    }
}