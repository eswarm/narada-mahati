package `in`.eswarm.mahati.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import `in`.eswarm.mahati.AppComponent
import `in`.eswarm.mahati.AppNavigation
import `in`.eswarm.mahati.db.DriverFactory // Added import
import `in`.eswarm.mahati.db.initializeDb  // Added import
import `in`.eswarm.mahati.theme.NaradaMQTTBrokerTheme // Corrected to your actual theme import path

fun main() = application {
    initializeDb(DriverFactory()) // Initialize DB for desktop

    Window(
        onCloseRequest = ::exitApplication,
        title = "Mahati KMP Desktop"
    ) {
        NaradaMQTTBrokerTheme {
            // Assuming AppComponent is correctly defined and needed here
            AppNavigation(AppComponent())
        }
    }
}
