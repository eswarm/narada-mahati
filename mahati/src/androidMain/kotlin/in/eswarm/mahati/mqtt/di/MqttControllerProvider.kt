package `in`.eswarm.mahati.mqtt.di

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import `in`.eswarm.mahati.MqttClientService
import `in`.eswarm.mahati.db.AppMqttMessage
import `in`.eswarm.mahati.db.MqttConnection
import `in`.eswarm.mahati.mqtt.common.MqttClientState
import `in`.eswarm.mahati.mqtt.service.MqttControllerContract
import `in`.eswarm.mahati.util.getAppComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


actual fun getMqttController(): MqttControllerContract {
    // On Android, we return a proxy object that handles service binding.
    return AndroidMqttControllerProxy
}

// We need a reference to the application context to bind to the service.
// This needs to be initialized once when the application starts.
@SuppressLint("StaticFieldLeak")
object AppContext {
    lateinit var context: Context
    fun init(context: Context) {
        this.context = context.applicationContext
    }
}

private object AndroidMqttControllerProxy : MqttControllerContract, ServiceConnection {

    // This holds the real service instance once we are bound to it.
    private val service = MutableStateFlow<MqttClientService?>(null)
    private val proxyScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _proxyConnectionStatesMap =
        MutableStateFlow<Map<String, MqttClientState>>(emptyMap())
    override val connectionStatesMap: StateFlow<Map<String, MqttClientState>> =
        _proxyConnectionStatesMap.asStateFlow()

    private val _proxyAllMessages = MutableSharedFlow<Pair<String, AppMqttMessage>>()
    override val allMessages: SharedFlow<Pair<String, AppMqttMessage>> =
        _proxyAllMessages.asSharedFlow()

    init {
        // Start and bind to the service as soon as this proxy is accessed.
        val intent = Intent(AppContext.context, MqttClientService::class.java)
        AppContext.context.startService(intent) // Ensure the service is started in the foreground
        AppContext.context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    override fun addConnection(config: MqttConnection) {
        service.value?.addConnection(config)
    }

    override fun removeConnection(clientID: String) {
        service.value?.removeConnection(clientID)
    }

    override suspend fun publish(
        clientID: String,
        topic: String,
        payload: ByteArray,
        qos: Int,
        retain: Boolean
    ): Boolean {
        return checkNotNull(service.value).publish(clientID, topic, payload, qos, retain)
    }

    override suspend fun subscribe(clientID: String, topicFilter: String, qos: Int): Boolean {
        return checkNotNull(service.value).subscribe(clientID, topicFilter, qos)
    }

    override suspend fun unsubscribe(
        clientID: String,
        topicFilter: String
    ): Boolean? {
        return checkNotNull(service.value).unsubscribe(clientID, topicFilter)
    }

    override fun shutdownAll() {
        AppContext.context.unbindService(this)
        proxyScope.cancel()
    }

    // --- ServiceConnection Callbacks ---
    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        val localBinder = binder as? MqttClientService.LocalBinder
        service.value = localBinder?.getService()

        // 3. Bridge the data from the real controller to the proxy flows.
        proxyScope.launch {
            val controller = service.value?.multiConnectionController

            // Once connected, bridge the flows permanently
            launch {
                controller?.connectionStatesMap?.collect { _proxyConnectionStatesMap.value = it }
            }
            launch {
                controller?.allMessages?.collect {
                    _proxyAllMessages.emit(it)
                }
            }

            val appComponent = AppContext.context.getAppComponent()
            val connections = appComponent.connectionRepo.getAllConnections()
            for (connection in connections) {
                addConnection(connection)
            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        service.value = null
    }
}
