package `in`.eswarm.mahati.db

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

internal var instance: MahatiDb? = null

fun getMahatiDb(): MahatiDb {
    if (instance == null) {
        error("MahatiDb has not been initialized. Call initializeDb(driverFactory) first.")
    }
    return instance!!
}

fun initializeDb(driverFactory: DriverFactory) {
    if (instance == null) {
        val driver = driverFactory.createDriver()
        // For Android, schema creation is handled by AndroidSqliteDriver's constructor when versions change.
        // For Desktop, we called MahatiDb.Schema.create(driver) in the desktop DriverFactory.
        instance = MahatiDb(
            driver = driver,
            MqttMessage.Adapter(
                directionAdapter = messageDirectionAdapter
            )
        )
    }
}
