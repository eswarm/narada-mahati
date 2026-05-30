package `in`.eswarm.narada.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import `in`.eswarm.narada.App
import `in`.eswarm.narada.AppComponent
import `in`.eswarm.narada.preferences.AppPreferences
import okio.Path.Companion.toPath

fun main() = application {
    // Create DataStore for desktop
    val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory
        .createWithPath(
            produceFile = {
                val userHome = System.getProperty("user.home")
                "$userHome/.narada/settings.preferences_pb".toPath()
            }
        )

    // Create AppPreferences
    val appPreferences = AppPreferences(dataStore)

    // Create the AppComponent
    val appComponent = AppComponent(appPreferences)

    Window(
        onCloseRequest = {
            appComponent.clear()
            exitApplication()
        },
        title = "Narada MQTT Broker Desktop"
    ) {
        App(appComponent)
    }
}

