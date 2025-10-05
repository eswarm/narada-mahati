package `in`.eswarm.mahati

import PermissionState
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import `in`.eswarm.mahati.chat.ChatScreen
import `in`.eswarm.mahati.connection.NewConnectionScreen
import `in`.eswarm.mahati.home.HomeScreen
import `in`.eswarm.mahati.navigation.Screen
import `in`.eswarm.mahati.topics.TopicSubscriptionScreen

@Composable
fun AppNavigation(
    appComponent: AppComponent,
    permissionState: PermissionState,
    requestPermission: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.NewConnection.route) {
            NewConnectionScreen(
                appComponent, {
                    navController.popBackStack()
                },
                {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                { navController.navigate(Screen.NewConnection.route) },
                { clientID -> navController.navigate(Screen.TopicSubscription.createRoute(clientID)) },
                appComponent,
                permissionState,
                requestPermission
            )
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
            val topicName = checkNotNull(backStackEntry.arguments?.getString("topicName"))
            val clientID = checkNotNull(backStackEntry.arguments?.getString("clientID"))
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
