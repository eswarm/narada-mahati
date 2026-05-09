package `in`.eswarm.narada.system

expect object BatteryOptimizationManager {
    fun isIgnoringBatteryOptimizations(): Boolean
    fun requestIgnoreBatteryOptimizations()
    val isSupported: Boolean
}

