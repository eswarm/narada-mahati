package `in`.eswarm.mahati.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import `in`.eswarm.mahati.AppNavigation // Assuming AppNavigation will be in commonMain
import `in`.eswarm.mahati.ui.theme.NaradaMQTTBrokerTheme // Assuming NaradaMQTTBrokerTheme will be in commonMain

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Mahati KMP Desktop" // You can customize this
    ) {
        NaradaMQTTBrokerTheme { // Apply your common theme
            AppNavigation() // Your shared navigation host from commonMain
        }
    }
}
