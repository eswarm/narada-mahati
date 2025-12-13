package `in`.eswarm.mahati

import PermissionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import `in`.eswarm.mahati.chat.ChatScreen
import `in`.eswarm.mahati.connection.ConnectionDetailsScreen
import `in`.eswarm.mahati.home.HomeScreen
import `in`.eswarm.mahati.navigation.DeepLinkDestination
import `in`.eswarm.mahati.navigation.Screen
import `in`.eswarm.mahati.settings.SettingsScreen
import `in`.eswarm.mahati.share.QrScannerScreen
import `in`.eswarm.mahati.share.QrScannerViewModel
import `in`.eswarm.mahati.topics.TopicSubscriptionScreen
import java.net.URLDecoder

@Composable
fun AppNavigation(
    appComponent: AppComponent,
    permissionState: PermissionState,
    requestPermission: () -> Unit,
    deepLinkDestination: DeepLinkDestination? = null,
    onDeepLinkHandled: () -> Unit = {}
) {
    val navController = rememberNavController()

    HandleDeepLink(navController, deepLinkDestination, onDeepLinkHandled)


    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.NewConnection.route) {
            ConnectionDetailsScreen(appComponent, {
                navController.popBackStack()
            }, {
                navController.popBackStack()
            })
        }
        composable(Screen.Home.route) {
            HomeScreen(
                { navController.navigate(Screen.NewConnection.route) },
                { navController.navigate(Screen.Settings.route) },
                { clientID -> navController.navigate(Screen.TopicSubscription.createRoute(clientID)) },
                { clientID ->
                    navController.navigate(
                        Screen.EditConnection.createRoute(
                            clientID
                        )
                    )
                },
                { navController.navigate(Screen.ScanQr.route) },
                appComponent,
                permissionState,
                requestPermission
            )
        }
        composable(Screen.ScanQr.route) {
            val viewModel: QrScannerViewModel =
                viewModel(factory = QrScannerViewModel.Factory(appComponent.connectionRepo))
            QrScannerScreen(onScanResult = {
                viewModel.onQrCodeScanned(it)
                navController.popBackStack()
            })
        }
        composable(
            Screen.EditConnection.route,
            arguments = listOf(navArgument("clientID") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientID = backStackEntry.arguments?.getString("clientID")

            ConnectionDetailsScreen(
                appComponent, {
                navController.popBackStack()
            }, {
                navController.popBackStack()
            }, clientID = clientID
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(
            Screen.TopicSubscription.route,
            arguments = listOf(navArgument("clientID") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientID = backStackEntry.arguments?.getString("clientID")
            if (clientID != null) {
                TopicSubscriptionScreen(
                    appComponent, clientID, onTopicClick = { topic ->
                        navController.navigate(
                            Screen.Chat.createRoute(
                                clientID, topic
                            )
                        )
                    })
            }
        }
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("clientID") { type = NavType.StringType },
                navArgument("topicName") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedTopicName = checkNotNull(backStackEntry.arguments?.getString("topicName"))
            val encodedClientID = checkNotNull(backStackEntry.arguments?.getString("clientID"))
            val clientID = URLDecoder.decode(encodedClientID, "UTF-8")
            val topicName = URLDecoder.decode(encodedTopicName, "UTF-8")

            if (topicName != null) {
                ChatScreen(
                    appComponent, clientID, topicName, appComponent.messageRepo
                )
            } else {
                // Handle error: topicName not found, perhaps navigate back or show error
            }
        }
    }
}

@Composable
private fun HandleDeepLink(
    navController: NavHostController,
    deepLinkDestination: DeepLinkDestination?,
    onDeepLinkHandled: () -> Unit
) {
    LaunchedEffect(deepLinkDestination) {
        if (deepLinkDestination != null) {
            when (deepLinkDestination) {
                is DeepLinkDestination.Chat -> {
                    navController.navigate(
                        Screen.Chat.createRoute(
                            deepLinkDestination.clientId, deepLinkDestination.topicName
                        )
                    )
                }
            }
            onDeepLinkHandled()
        }
    }
}
