package `in`.eswarm.mahati.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DriverFactory { // No constructor parameter needed for basic desktop version
    actual fun createDriver(): SqlDriver {
        val dbFile = File(System.getProperty("user.home"), ".mahati/mahati.db")
        if (!dbFile.parentFile.exists()) {
            dbFile.parentFile.mkdirs()
        }

        Class.forName("org.sqlite.JDBC")

        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        MahatiDb.Schema.create(driver) // Create schema if it doesn't exist
        return driver
    }
}