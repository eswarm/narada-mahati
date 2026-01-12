package `in`.eswarm.mahati.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import `in`.eswarm.mahati.AppComponent
import `in`.eswarm.mahati.AppNavigation
import `in`.eswarm.mahati.data.data.SettingsDataStore
import `in`.eswarm.mahati.db.DriverFactory
import `in`.eswarm.mahati.db.initializeDb
import `in`.eswarm.mahati.theme.NaradaMQTTBrokerTheme
import java.io.File

fun main() = application {
    // Setup Database
    initializeDb(DriverFactory())

    // Setup Settings DataStore for Desktop
    val settingsDataStore = SettingsDataStore(
        PreferenceDataStoreFactory.create {
            File(System.getProperty("user.home"), "mahati_settings.preferences_pb")
        }
    )

    // Create the AppComponent
    val appComponent = AppComponent(settingsDataStore)

    Window(
        onCloseRequest = ::exitApplication,
        title = "Mahati KMP Desktop"
    ) {
        NaradaMQTTBrokerTheme {
            AppNavigation(appComponent)
        }
    }
}
