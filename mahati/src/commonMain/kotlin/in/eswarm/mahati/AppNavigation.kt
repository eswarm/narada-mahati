package `in`.eswarm.mahati

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import `in`.eswarm.mahati.chat.ChatScreen
import `in`.eswarm.mahati.connection.ConnectionScreen
import `in`.eswarm.mahati.home.HomeScreen
import `in`.eswarm.mahati.navigation.Screen
import `in`.eswarm.mahati.topics.TopicSubscriptionScreen

@Composable
fun AppNavigation(appComponent: AppComponent) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Connection.route) {
            ConnectionScreen(
                appComponent
                // onConnectionSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Connection.route) { inclusive = true } } }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen({ navController.navigate(Screen.Connection.route) }, {}, appComponent)
        }
        composable(Screen.TopicSubscription.route) {
            TopicSubscriptionScreen(
                appComponent
                // onSubscribed = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("topicName") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicName = backStackEntry.arguments?.getString("topicName")
            if (topicName != null) {
                ChatScreen(
                    appComponent, topicName, "" // TODO: Fix this.
                    // onNavigateBack = { navController.popBackStack() }
                )
            } else {
                // Handle error: topicName not found, perhaps navigate back or show error
            }
        }
    }
}
