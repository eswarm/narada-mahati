package `in`.eswarm.narada

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import `in`.eswarm.narada.launch.LaunchScreen
import `in`.eswarm.narada.launch.LaunchViewModel
import `in`.eswarm.narada.settings.SettingsScreen
import `in`.eswarm.narada.settings.SettingsViewModelFactory
import `in`.eswarm.narada.ui.theme.NaradaMQTTBrokerTheme

@Composable
fun App(appComponent: AppComponent) {
    NaradaMQTTBrokerTheme {
        val navController = rememberNavController()

        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background // Changed to colorScheme
        ) {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    LaunchScreen(
                        viewModel(
                            factory = LaunchViewModel.Factory(
                                appComponent.logStream,
                                appComponent.mqttServerListener,
                                appComponent.appPreferences
                            )
                        ), navController
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        viewModel(
                            factory = SettingsViewModelFactory(
                                appComponent.appPreferences
                            )
                        )
                    )
                }
            }
        }
    }

}