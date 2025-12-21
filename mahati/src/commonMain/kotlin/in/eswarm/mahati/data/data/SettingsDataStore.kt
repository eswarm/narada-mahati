package `in`.eswarm.mahati.data.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsDataStore(private val dataStore: DataStore<Preferences>) {

    val autoReconnect: Flow<Boolean>
        get() = dataStore.data.map {
            it[AUTO_RECONNECT] ?: false
        }

    suspend fun setAutoReconnect(value: Boolean) {
        dataStore.edit {
            it[AUTO_RECONNECT] = value
        }
    }

    val logs: Flow<Set<String>>
        get() = dataStore.data.map { it[LOGS] ?: emptySet() }

    suspend fun addLog(log: String) {
        dataStore.edit { prefs ->
            val currentLogs = prefs[LOGS] ?: emptySet()
            // To prevent unbounded growth, let's keep only the last 1000 logs
            val sortedLogs = currentLogs.sortedDescending()
            val newLogs = sortedLogs.take(999).toMutableSet()
            newLogs.add(log)
            prefs[LOGS] = newLogs
        }
    }

    suspend fun clearLogs() {
        dataStore.edit { it[LOGS] = emptySet() }
    }

    companion object {
        val AUTO_RECONNECT = booleanPreferencesKey("auto_reconnect")
        val LOGS = stringSetPreferencesKey("logs")
    }
}
