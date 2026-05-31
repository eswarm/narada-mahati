package `in`.eswarm.mahati.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbFile = File(System.getProperty("user.home"), ".mahati/mahati.db")
        if (!dbFile.parentFile.exists()) {
            dbFile.parentFile.mkdirs()
        }

        Class.forName("org.sqlite.JDBC")

        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")

        // Enable WAL mode for better concurrency
        driver.execute(null, "PRAGMA journal_mode=WAL", 0, null)
        // Increase busy timeout to 30 seconds
        driver.execute(null, "PRAGMA busy_timeout=30000", 0, null)

        // Create schema if tables don't exist
        try {
            MahatiDb.Schema.create(driver)
        } catch (e: Exception) {
            // Tables already exist, which is fine
            println("Database schema already exists: ${e.message}")
        }

        return driver
    }
}