package `in`.eswarm.narada.preferences

import `in`.eswarm.narada.mqtt.ServerProperties
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random

class AppPreferences(
    private val dataStore: DataStore<Preferences>,
) {

    val mqttPort: Flow<Int>
        get() {
            return dataStore.data.map { it[MQTT_PORT] ?: MQTT_PORT_DEFAULT }
        }

    val wsEnabled: Flow<Boolean>
        get() {
            return dataStore.data.map { it[WS_ENABLED] ?: WS_ENABLED_DEFAULT }
        }

    val wsPort: Flow<Int>
        get() {
            return dataStore.data.map { it[WS_PORT] ?: WS_PORT_DEFAULT }
        }

    val wsPath: Flow<String>
        get() {
            return dataStore.data.map { it[WS_PATH] ?: WS_PATH_DEFAULT }
        }

    val authEnabled: Flow<Boolean>
        get() {
            return dataStore.data.map { it[AUTH_ENABLED] ?: AUTH_ENABLED_DEFAULT }
        }

    val userName: Flow<String>
        get() {
            return dataStore.data.map { it[UNAME] ?: UNAME_DEFAULT }
        }

    val password: Flow<String>
        get() {
            return dataStore.data.map { it[PWD] ?: PWD_DEFAULT }
        }

    val isServerStarted: Flow<Boolean>
        get() {
            return dataStore.data.map { it[SERVER_STARTED] ?: false }
        }

    val logs: Flow<Set<String>>
        get() {
            return dataStore.data.map { it[LOGS] ?: emptySet() }
        }

    suspend fun getServerProperties(): ServerProperties {
        return dataStore.data.map { preferences ->
            val mqttPort = preferences[MQTT_PORT] ?: MQTT_PORT_DEFAULT
            val wsEnabled = preferences[WS_ENABLED] ?: WS_ENABLED_DEFAULT
            val wsPort = preferences[WS_PORT] ?: WS_PORT_DEFAULT
            val wsPath = preferences[WS_PATH] ?: WS_PATH_DEFAULT
            val authEnabled = preferences[AUTH_ENABLED] ?: AUTH_ENABLED_DEFAULT
            val userName = preferences[UNAME] ?: UNAME_DEFAULT
            val password = preferences[PWD] ?: PWD_DEFAULT
            return@map ServerProperties(
                mqttPort, wsEnabled, wsPort, wsPath, authEnabled, userName, password
            )
        }.first()
    }

    suspend fun addLog(log: String) {
        val simpleDateFormat = SimpleDateFormat("hh::mm::ss", Locale.getDefault())
        dataStore.edit { prefs ->
            val currentLogs = prefs[LOGS] ?: emptySet()
            // To prevent unbounded growth, let's keep only the last 1000 logs
            val sortedLogs = currentLogs.sortedDescending()
            val newLogs = sortedLogs.take(999).toMutableSet()
            newLogs.add("${simpleDateFormat.format(System.currentTimeMillis())} $log")
            prefs[LOGS] = newLogs
        }
    }

    suspend fun clearLogs() {
        dataStore.edit { it[LOGS] = emptySet() }
    }

    suspend fun setMqttPort(value: Int) {
        dataStore.edit { it[MQTT_PORT] = value }
    }

    suspend fun setWSEnabled(value: Boolean) {
        dataStore.edit { it[WS_ENABLED] = value }
    }

    suspend fun setWSPort(value: Int) {
        dataStore.edit { it[WS_PORT] = value }
    }

    suspend fun setWSPath(value: String) {
        dataStore.edit { it[WS_PATH] = value }
    }

    suspend fun setAuthEnabled(value: Boolean) {
        dataStore.edit { it[AUTH_ENABLED] = value }
    }

    suspend fun setUsername(value: String) {
        dataStore.edit { it[UNAME] = value }
    }

    suspend fun setPassword(value: String) {
        dataStore.edit { it[PWD] = value }
    }

    suspend fun setServerStarted() {
        dataStore.edit { it[SERVER_STARTED] = true }
    }

    suspend fun setServerStopped() {
        dataStore.edit { it[SERVER_STARTED] = false }
    }

    suspend fun setPassword() {
        val randomNum: Int = Random.nextInt(100000, 1000000)
        dataStore.edit { preferences ->
            if (!preferences.contains(PWD)) {
                preferences[PWD] = PWD_DEFAULT + randomNum
            }
        }
    }

    companion object {
        val MQTT_PORT = intPreferencesKey("mqtt_port")
        val WS_ENABLED = booleanPreferencesKey("ws_enabled")
        val WS_PORT = intPreferencesKey("ws_port")
        val WS_PATH = stringPreferencesKey("ws_path")
        val AUTH_ENABLED = booleanPreferencesKey("auth_enabled")
        val UNAME = stringPreferencesKey("username")
        val PWD = stringPreferencesKey("password")
        val SERVER_STARTED = booleanPreferencesKey("server_started")
        val LOGS = stringSetPreferencesKey("logs")

        const val MQTT_PORT_DEFAULT = 1883
        const val WS_ENABLED_DEFAULT = true
        const val WS_PORT_DEFAULT = 8080
        const val WS_PATH_DEFAULT = "/mqtt"
        const val AUTH_ENABLED_DEFAULT = false
        const val UNAME_DEFAULT = "narada"
        const val PWD_DEFAULT = "narada"
    }
}