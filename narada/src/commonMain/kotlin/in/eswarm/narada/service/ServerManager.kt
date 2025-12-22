package `in`.eswarm.narada.service

import kotlinx.coroutines.flow.StateFlow

interface ServerManager {
    val isRunning: StateFlow<Boolean>
    val clientsConnected: Int
    fun start()
    fun stop()
}

expect fun getServerManager(): ServerManager
