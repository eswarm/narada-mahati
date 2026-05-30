package `in`.eswarm.narada.service

import `in`.eswarm.narada.mqtt.MQTTWrapper
import `in`.eswarm.narada.preferences.AppPreferences
import kotlinx.coroutines.flow.StateFlow

interface ServerManager {
    val isRunning: StateFlow<Boolean>
    val clientsConnected: StateFlow<Int>
    fun start()
    fun stop()
}

expect fun getServerManager(mqttWrapper: MQTTWrapper, appPreferences: AppPreferences): ServerManager

