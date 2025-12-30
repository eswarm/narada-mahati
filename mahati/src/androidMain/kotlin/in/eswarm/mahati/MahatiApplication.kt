package `in`.eswarm.mahati

import android.app.Application
import androidx.datastore.preferences.preferencesDataStore
import `in`.eswarm.mahati.data.data.SettingsDataStore
import `in`.eswarm.mahati.db.DriverFactory
import `in`.eswarm.mahati.db.initializeDb
import `in`.eswarm.mahati.mqtt.di.AppContext

class MahatiApplication : Application() {

    lateinit var appComponent: AppComponent
    val prefStore by preferencesDataStore(name = "settings")

    override fun onCreate() {
        super.onCreate()
        initializeDb(DriverFactory(this))
        AppContext.init(this)
        val settingsStore = SettingsDataStore(prefStore)
        appComponent = AppComponent(settingsStore)
    }

    override fun onTerminate() {
        super.onTerminate()
        appComponent.clear()
    }
}
