package `in`.eswarm.narada.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

object BatteryOptimizationHelper {

    /**
     * Check if the app is exempt from battery optimization
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * Request the user to disable battery optimization for this app
     * This is required for MQTT keepalive during Doze mode
     */
    fun requestIgnoreBatteryOptimizations(context: Context) {
        if (!isIgnoringBatteryOptimizations(context)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    /**
     * Open battery optimization settings screen
     */
    fun openBatteryOptimizationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}

