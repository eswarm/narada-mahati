package `in`.eswarm.narada.util

import `in`.eswarm.narada.AppComponent
import `in`.eswarm.narada.NaradaApplication
import `in`.eswarm.narada.preferences.AppPreferences
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

fun Context.getAppComponent(): AppComponent {
    val application = applicationContext as NaradaApplication
    return application.appComponent
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val Context.preferences: AppPreferences
    get() {
        return AppPreferences(dataStore)
    }

val appBgScope: CoroutineScope
    get() {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }