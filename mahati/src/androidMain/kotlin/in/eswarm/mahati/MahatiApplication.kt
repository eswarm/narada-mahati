package `in`.eswarm.mahati

import android.app.Application
import android.content.Intent
import androidx.datastore.preferences.preferencesDataStore
import `in`.eswarm.mahati.data.data.SettingsDataStore
import `in`.eswarm.mahati.db.DriverFactory
import `in`.eswarm.mahati.db.initializeDb
import `in`.eswarm.mahati.mqtt.di.AppContext
import `in`.eswarm.mahati.util.NotificationUtil
import kotlinx.coroutines.runBlocking

class MahatiApplication : Application() {

    lateinit var appComponent: AppComponent
    private val prefStore by preferencesDataStore(name = "settings")

    override fun onCreate() {
        super.onCreate()
        // Initialize the database and AppContext first
        initializeDb(DriverFactory(this))
        AppContext.init(this)

        // Create the notification channel
        runBlocking {
            NotificationUtil.createNotificationChannel(this@MahatiApplication)
        }

        // Define the notification function required by the controller
        val sendNotification: (String, String, String, String) -> Unit =
            { title, message, clientId, topic ->
                NotificationUtil.sendNotification(this, title, message, clientId, topic)
            }

        // Initialize the AppComponent, passing the notification function
        val settingsStore = SettingsDataStore(prefStore)
        appComponent = AppComponent(settingsStore, sendNotification)

        // Start the MqttClientService to run in the background
        val serviceIntent = Intent(this, MqttClientService::class.java)
        startService(serviceIntent)
    }

    override fun onTerminate() {
        super.onTerminate()
        appComponent.clear()
    }
}
