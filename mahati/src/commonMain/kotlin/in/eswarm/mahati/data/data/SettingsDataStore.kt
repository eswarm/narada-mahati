package `in`.eswarm.mahati.data.data

import `in`.eswarm.mahati.db.MahatiDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull

class SettingsDataStore(private val db: MahatiDb) {

    val autoReconnect: Flow<Boolean>
        get() = db.settingsQueries.getBool(AUTO_RECONNECT_KEY)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { if(it != null) { it.value_bool == 1L } else { false }  }

    suspend fun setAutoReconnect(value: Boolean) {
        withContext(Dispatchers.IO) {
            db.settingsQueries.insertOrUpdateBool(AUTO_RECONNECT_KEY, if (value) 1L else 0L)
        }
    }

    val ignoreBatteryOptimization: Flow<Boolean>
        get() = db.settingsQueries.getBool(IGNORE_BATTERY_OPTIMIZATION_KEY)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { if(it != null) { it.value_bool == 1L } else { false }  }

    suspend fun setIgnoreBatteryOptimization(value: Boolean) {
        withContext(Dispatchers.IO) {
            db.settingsQueries.insertOrUpdateBool(IGNORE_BATTERY_OPTIMIZATION_KEY, if (value) 1L else 0L)
        }
    }

    val wakeLock: Flow<Boolean>
        get() = db.settingsQueries.getBool(WAKELOCK_KEY)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { if(it != null) { it.value_bool == 1L } else { false }  }

    suspend fun setWakeLock(value: Boolean) {
        withContext(Dispatchers.IO) {
            db.settingsQueries.insertOrUpdateBool(WAKELOCK_KEY, if (value) 1L else 0L)
        }
    }

    companion object {
        private const val AUTO_RECONNECT_KEY = "auto_reconnect"
        private const val IGNORE_BATTERY_OPTIMIZATION_KEY = "ignore_battery_optimization"
        private const val WAKELOCK_KEY = "wakelock"
    }
}
