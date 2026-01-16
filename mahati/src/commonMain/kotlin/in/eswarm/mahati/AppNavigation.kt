package `in`.eswarm.mahati

import PermissionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import `in`.eswarm.mahati.chat.ChatScreen
import `in`.eswarm.mahati.connection.ConnectionDetailsScreen
import `in`.eswarm.mahati.home.HomeScreen
import `in`.eswarm.mahati.log.LogScreen
import `in`.eswarm.mahati.navigation.DeepLinkDestination
import `in`.eswarm.mahati.navigation.Route
import `in`.eswarm.mahati.settings.SettingsScreen
import `in`.eswarm.mahati.share.QrScannerScreen
import `in`.eswarm.mahati.share.QrScannerViewModel
import `in`.eswarm.mahati.topics.TopicSubscriptionScreen

@Composable
fun AppNavigation(
    appComponent: AppComponent,
    permissionState: PermissionState = PermissionState.GRANTED,
    requestPermission: () -> Unit = {},
    deepLinkDestination: DeepLinkDestination? = null,
    onDeepLinkHandled: () -> Unit = {}
) {
    val navController = rememberNavController()

    HandleDeepLink(navController, deepLinkDestination, onDeepLinkHandled)


    NavHost(navController = navController, startDestination = Route.Home) {
        composable<Route.NewConnection> {
            ConnectionDetailsScreen(appComponent, {
                navController.popBackStack()
            }, {
                navController.popBackStack()
            })
        }
        composable<Route.Home> {
            HomeScreen(
                { navController.navigate(Route.NewConnection) },
                { navController.navigate(Route.Settings) },
                { clientID -> navController.navigate(Route.TopicSubscription(clientID)) },
                { clientID ->
                    navController.navigate(
                        Route.EditConnection(clientID)
                    )
                },
                { navController.navigate(Route.QRCode) },
                { navController.navigate(Route.Log) },
                appComponent,
                permissionState,
                requestPermission
            )
        }
        composable<Route.Log> {
            LogScreen(appComponent)
        }
        composable<Route.QRCode> {
            val viewModel: QrScannerViewModel =
                viewModel(factory = QrScannerViewModel.Factory(appComponent.connectionRepo))
            QrScannerScreen(onScanResult = {
                viewModel.onQrCodeScanned(it)
                navController.popBackStack()
            })
        }
        composable<Route.EditConnection> { backStackEntry ->
            val editConnection: Route.EditConnection = backStackEntry.toRoute()

            ConnectionDetailsScreen(
                appComponent, {
                    navController.popBackStack()
                }, {
                    navController.popBackStack()
                }, clientID = editConnection.clientID
            )
        }
        composable<Route.Settings> {
            SettingsScreen()
        }
        composable<Route.TopicSubscription> { backStackEntry ->

            val topicSubscription: Route.TopicSubscription = backStackEntry.toRoute()

            TopicSubscriptionScreen(
                appComponent, topicSubscription.clientID, onTopicClick = { topic ->
                    navController.navigate(
                        Route.Chat(
                            topicSubscription.clientID, topic
                        )
                    )
                })
        }
        composable<Route.Chat> { backStackEntry ->
            val chat: Route.Chat = backStackEntry.toRoute()
            ChatScreen(
                appComponent, chat.clientID, chat.topicName, appComponent.messageRepo
            )
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
                        Route.Chat(deepLinkDestination.clientId, deepLinkDestination.topicName)
                    )
                }
            }
            onDeepLinkHandled()
        }
    }
}
