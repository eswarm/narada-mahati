package `in`.eswarm.mahati.system

expect object BatteryOptimizationManager {
    fun isIgnoringBatteryOptimizations(): Boolean
    fun requestIgnoreBatteryOptimizations()
    val isSupported: Boolean
}

