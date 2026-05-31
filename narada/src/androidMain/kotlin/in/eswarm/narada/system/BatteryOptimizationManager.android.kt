package `in`.eswarm.narada.system

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import `in`.eswarm.narada.util.AppContext

actual object BatteryOptimizationManager {
    actual fun isIgnoringBatteryOptimizations(): Boolean {
        val context = AppContext.context ?: return true
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    actual fun requestIgnoreBatteryOptimizations() {
        val context = AppContext.context ?: return
        if (isIgnoringBatteryOptimizations()) return
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual val isSupported: Boolean = true
}

