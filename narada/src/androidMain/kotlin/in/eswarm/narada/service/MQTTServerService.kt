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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MQTTServerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            START -> {
                init()
                return START_STICKY
            }
            STOP -> {
                stopService()
                return START_NOT_STICKY
            }
        }
        return START_STICKY
    }

    private fun stopService() {
        MQTTWrapper.stopMoquette()
        stopForeground(true)
        stopSelf()
    }

    private fun init() {
        serviceScope.launch {
            val serverProperties = application.preferences.getServerProperties()
            MQTTWrapper.startMoquette(
                AppComponent.INSTANCE.mqttServerListener,
                AppComponent.INSTANCE.logStream,
                serverProperties
            )

            withContext(Dispatchers.Main) {
                val pendingIntent: PendingIntent =
                    Intent(this@MQTTServerService, LaunchActivity::class.java).let { notificationIntent ->
                        PendingIntent.getActivity(
                            this@MQTTServerService,
                            0,
                            notificationIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    }

                val notification: Notification = NotificationCompat.Builder(this@MQTTServerService, FG_SERVICE_CHANNEL)
                    .setContentTitle(getText(R.string.notification_mqtt_service_title))
                    .setContentText(getText(R.string.notification_mqtt_service_content))
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .setTicker(getText(R.string.notification_mqtt_ticker))
                    .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
                    .build()

                startForeground(NOT_SERVICE_ID, notification)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        stopService()
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