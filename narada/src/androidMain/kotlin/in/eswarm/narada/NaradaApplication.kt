package `in`.eswarm.narada

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import `in`.eswarm.narada.preferences.AppPreferences
import `in`.eswarm.narada.util.AppContext

class NaradaApplication : Application() {

    val prefStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        AppContext.context = this

        val preferences = AppPreferences(prefStore)
        appComponent = AppComponent(preferences)
    }

    override fun onTerminate() {
        super.onTerminate()
        appComponent.clear()
    }
}