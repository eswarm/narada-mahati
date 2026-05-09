package `in`.eswarm.mahati.system

actual object BatteryOptimizationManager {
    actual fun isIgnoringBatteryOptimizations(): Boolean = true

    actual fun requestIgnoreBatteryOptimizations() {
        // No-op
    }

    actual val isSupported: Boolean = false
}

