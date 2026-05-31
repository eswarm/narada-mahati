package `in`.eswarm.mahati

import android.app.Application
import android.content.Intent
import `in`.eswarm.mahati.db.DriverFactory
import `in`.eswarm.mahati.db.initializeDb
import `in`.eswarm.mahati.mqtt.di.AppContext
import `in`.eswarm.mahati.util.NotificationUtil
import kotlinx.coroutines.runBlocking

class MahatiApplication : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        // Initialize the database and AppContext first
        initializeDb(DriverFactory(this))
        AppContext.init(this)

        // Create the notification channel
        runBlocking {
            NotificationUtil.createNotificationChannel(this@MahatiApplication)
            NotificationUtil.createMessageChannel(this@MahatiApplication)
        }

        // Define the notification function required by the controller
        val sendNotification: (String, String, String, String) -> Unit =
            { title, message, clientId, topic ->
                NotificationUtil.sendNotification(this, title, message, clientId, topic)
            }

        // Define the action to start the foreground service
        val onConnectionAction: () -> Unit = {
            val serviceIntent = Intent(this, MqttClientService::class.java)
            startForegroundService(serviceIntent)
        }

        // Initialize the AppComponent, passing the notification function and the service starter callback
        appComponent = AppComponent(sendNotification, onConnectionAction)
    }

    override fun onTerminate() {
        super.onTerminate()
        appComponent.clear()
    }
}
