package `in`.eswarm.narada.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import `in`.eswarm.narada.AppComponent
import `in`.eswarm.narada.R
import `in`.eswarm.narada.home.HomeActivity
import `in`.eswarm.narada.util.BatteryOptimizationHelper
import `in`.eswarm.narada.util.NotificationUtil.FG_SERVICE_CHANNEL
import `in`.eswarm.narada.util.getAppComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MQTTServerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    lateinit var appComponent: AppComponent
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAsForeground()
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

    private fun startAsForeground() {
        val pendingIntent: PendingIntent = Intent(
            this@MQTTServerService, HomeActivity::class.java
        ).let { notificationIntent ->
            PendingIntent.getActivity(
                this@MQTTServerService,
                0,
                notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification: Notification =
            NotificationCompat.Builder(this@MQTTServerService, FG_SERVICE_CHANNEL)
                .setContentTitle(getText(R.string.notification_mqtt_service_title))
                .setContentText(getText(R.string.notification_mqtt_service_content))
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.notification_mqtt_ticker))
                .build()

        startForeground(NOT_SERVICE_ID, notification)
    }

    private fun stopService() {
        serviceScope.launch {
            appComponent.appPreferences.setServerStopped()
            appComponent.mqttWrapper.stopMoquette()
            releaseWakeLock()
            withContext(Dispatchers.Main) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun init() {
        serviceScope.launch {
            appComponent.appPreferences.setServerStarted()
            val serverProperties = appComponent.appPreferences.getServerProperties()

            val useWakeLock = appComponent.appPreferences.wakeLock.first()
            if (useWakeLock) {
                acquireWakeLock()
            }

            appComponent.mqttWrapper.startMoquette(
                serverProperties
            )
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        appComponent = getAppComponent()
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "Narada::MqttServerWakeLock"
            )
            wakeLock?.acquire(10 * 60 * 60 * 1000L) // 10 hours max
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
        if (!BatteryOptimizationHelper.isIgnoringBatteryOptimizations(this)) {
            try {
                BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(this)
            } catch (e: Exception) {
                // Permission not granted or other issue
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService()
        serviceScope.cancel()
    }

    companion object {
        const val NOT_SERVICE_ID = 987
        const val START = "start"
        const val STOP = "stop"

        fun start(context: Context) {
            val intent = Intent(context, MQTTServerService::class.java)
            intent.action = START
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MQTTServerService::class.java)
            intent.action = STOP
            ContextCompat.startForegroundService(context, intent)
        }
    }
}