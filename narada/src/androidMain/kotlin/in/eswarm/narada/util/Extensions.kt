package `in`.eswarm.narada.util

import `in`.eswarm.narada.preferences.AppPreferences
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val Context.preferences: AppPreferences
    get() {
        // TODO :: check if multiple objects are created here.
        return AppPreferences(dataStore)
    }

val appBgScope: CoroutineScope
    get() {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }