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
import `in`.eswarm.narada.home.LaunchScreen
import `in`.eswarm.narada.home.HomeViewModel
import `in`.eswarm.narada.log.LogView
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
                    val launchViewModel: HomeViewModel = viewModel(
                        factory = HomeViewModel.Factory(
                            appComponent.logStream,
                            appComponent.appPreferences,
                            appComponent.serverManager
                        )
                    )
                    LaunchScreen(
                        launchViewModel,
                        navController
                    ) { logs ->
                        LogView(logs = logs)
                    }
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