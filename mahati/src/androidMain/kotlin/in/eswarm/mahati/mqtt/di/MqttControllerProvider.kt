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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual fun getMqttController(): MqttControllerContract {
    return AndroidMqttControllerProxy
}

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

    init {
        // Start and bind to the service as soon as this proxy is accessed.
        val intent = Intent(AppContext.context, MqttClientService::class.java)
        // Ensure the service is started in the foreground
        AppContext.context.startService(intent)
        // Bind to it
        AppContext.context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    override val connectionStatesMap: StateFlow<Map<String, MqttClientState>>
        get() = service.value?.multiConnectionController?.connectionStatesMap
            ?: MutableStateFlow<Map<String, MqttClientState>>(emptyMap()).asStateFlow()

    override val allMessages: SharedFlow<Pair<String, AppMqttMessage>>?
        get() = service.value?.multiConnectionController?.allMessages


    override fun addConnection(config: MqttConnection) {
        service.value?.multiConnectionController?.addConnection(config)
    }

    override fun removeConnection(connectionId: String) {
        service.value?.multiConnectionController?.removeConnection(connectionId)
    }

    override fun publish(
        connectionId: String, topic: String, payload: ByteArray, qos: Int, retain: Boolean
    ) {
        service.value?.multiConnectionController?.publish(connectionId, topic, payload, qos, retain)
    }

    override fun subscribe(connectionId: String, topicFilter: String, qos: Int) {
        service.value?.multiConnectionController?.subscribe(connectionId, topicFilter, qos)
    }

    override fun shutdownAll() {
        service.value?.multiConnectionController?.shutdownAll()
        AppContext.context.unbindService(this)
    }

    // --- ServiceConnection Callbacks ---
    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        val localBinder = binder as? MqttClientService.LocalBinder
        service.value = localBinder?.getService()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        service.value = null
    }
}
