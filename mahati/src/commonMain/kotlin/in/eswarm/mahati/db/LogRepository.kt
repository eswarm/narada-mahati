package `in`.eswarm.mahati.db

import `in`.eswarm.shared.LogProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList

class LogRepository(private val db: MahatiDb) : LogProvider {

    override val logs: Flow<List<String>> // Changed from Set to List
        get() = db.logQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)

    override suspend fun addLog(msg: String) {
        withContext(Dispatchers.IO) {
            db.logQueries.transaction {
                db.logQueries.insert(msg)
                db.logQueries.trimOldLogs(1000L)
            }
        }
    }

    override suspend fun clearLogs() {
        withContext(Dispatchers.IO) {
            db.logQueries.deleteAll()
        }
    }
}
