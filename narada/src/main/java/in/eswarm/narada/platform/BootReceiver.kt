package `in`.eswarm.narada.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import `in`.eswarm.narada.mqtt.MQTTService
import `in`.eswarm.narada.util.preferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val preferences = context.preferences
        Log.i("BootReceiver", "Recevied broadcast event")

        if (intent.action != null && intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i("BootReceiver", "Recevied boot event")
            // Restart the services.
            runBlocking {
                if (preferences.isServerStarted.first()) {
                    Log.i("BootReceiver", "Starting server")
                    MQTTService.start(context)
                }
            }
        }
    }

}