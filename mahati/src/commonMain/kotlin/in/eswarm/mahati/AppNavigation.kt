package `in`.eswarm.mahati

import PermissionState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import `in`.eswarm.mahati.chat.ChatScreen
import `in`.eswarm.mahati.connection.ConnectionDetailsScreen
import `in`.eswarm.mahati.home.ConnectionListContent
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
    permissionState: PermissionState? = null,
    requestPermission: () -> Unit = {},
    deepLinkDestination: DeepLinkDestination? = null,
    onDeepLinkHandled: () -> Unit = {}
) {
    BoxWithConstraints {
        val isExpanded = maxWidth > 600.dp

        if (isExpanded) {
            DesktopNavigation(appComponent, permissionState, requestPermission, deepLinkDestination, onDeepLinkHandled)
        } else {
            MobileNavigation(appComponent, permissionState, requestPermission, deepLinkDestination, onDeepLinkHandled)
        }
    }
}

@Composable
fun MobileNavigation(
    appComponent: AppComponent,
    permissionState: PermissionState?,
    requestPermission: () -> Unit,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopNavigation(
    appComponent: AppComponent,
    permissionState: PermissionState?,
    requestPermission: () -> Unit,
    deepLinkDestination: DeepLinkDestination? = null,
    onDeepLinkHandled: () -> Unit = {}
) {
    val detailNavController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mahati : MQTT Client") },
                actions = {
                    IconButton(onClick = { detailNavController.navigate(Route.Log) }) {
                        Icon(Icons.Default.Article, contentDescription = "View Logs")
                    }
                }
            )
        }
    ) { innerPadding ->
        Row(Modifier.fillMaxSize().padding(innerPadding)) {
            // Left Pane: Connection List
            ConnectionListContent(
                modifier = Modifier.weight(0.4f),
                appComponent = appComponent,
                onNavigateToNewConnection = { detailNavController.navigate(Route.NewConnection) },
                onNavigateToSettings = { detailNavController.navigate(Route.Settings) },
                onNavigateToConnectionDetails = { clientID -> detailNavController.navigate(Route.TopicSubscription(clientID)) },
                onEditConnection = { clientID -> detailNavController.navigate(Route.EditConnection(clientID)) },
                onConnectionsUpdated = { isEmpty -> // Add this new callback
                    if (isEmpty) {
                        detailNavController.navigate(Route.NewConnection) {
                            // Ensure there's only one "New Connection" screen on the backstack
                            popUpTo(detailNavController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            )

            VerticalDivider()

            // Right Pane: Detail View
            NavHost(
                navController = detailNavController,
                startDestination = Route.NoSelection,
                modifier = Modifier.weight(0.6f)
            ) {
                composable<Route.NoSelection> {
                    // Empty placeholder for when no item is selected
                }
                composable<Route.NewConnection> {
                    ConnectionDetailsScreen(appComponent, {
                        detailNavController.popBackStack()
                    }, {
                        detailNavController.popBackStack()
                    })
                }
                composable<Route.Log> {
                    LogScreen(appComponent)
                }
                composable<Route.EditConnection> { backStackEntry ->
                    val editConnection: Route.EditConnection = backStackEntry.toRoute()
                    ConnectionDetailsScreen(
                        appComponent, {
                            detailNavController.popBackStack()
                        }, {
                            detailNavController.popBackStack()
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
                            detailNavController.navigate(
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
