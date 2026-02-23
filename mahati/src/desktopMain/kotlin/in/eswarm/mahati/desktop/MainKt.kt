package `in`.eswarm.mahati.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import `in`.eswarm.mahati.AppComponent
import `in`.eswarm.mahati.AppNavigation
import `in`.eswarm.mahati.db.DriverFactory
import `in`.eswarm.mahati.db.initializeDb
import `in`.eswarm.mahati.theme.NaradaMQTTBrokerTheme

fun main() = application {

    // 1. TODO : Initialize the logging system FIRST.

    // Setup Database
    initializeDb(DriverFactory())

    // Create the AppComponent
    val appComponent = AppComponent()


    Window(
        onCloseRequest = ::exitApplication,
        title = "Mahati KMP Desktop"
    ) {
        NaradaMQTTBrokerTheme {
            AppNavigation(appComponent)
        }
    }
}
