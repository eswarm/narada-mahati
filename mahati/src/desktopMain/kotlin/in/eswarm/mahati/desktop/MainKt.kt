package `in`.eswarm.mahati.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import `in`.eswarm.mahati.AppComponent
import `in`.eswarm.mahati.AppNavigation
import `in`.eswarm.mahati.theme.NaradaMQTTBrokerTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Mahati KMP Desktop" // You can customize this
    ) {
        NaradaMQTTBrokerTheme { // Apply your common theme
            AppNavigation(AppComponent()) // Your shared navigation host from commonMain
        }
    }
}
