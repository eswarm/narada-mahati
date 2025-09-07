package `in`.eswarm.mahati

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import `in`.eswarm.mahati.chat.ChatScreen
import `in`.eswarm.mahati.connection.ConnectionScreen
import `in`.eswarm.mahati.navigation.Screen // Import your Screen routes
import `in`.eswarm.mahati.ui.theme.NaradaMQTTBrokerTheme

import `in`.eswarm.mahati.home.HomeScreen
import `in`.eswarm.mahati.topics.TopicSubscriptionScreen

class Main : ComponentActivity() { // This class now hosts the navigation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NaradaMQTTBrokerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val appComponent = (this.applicationContext as MahatiApplication).appComponent
                    AppNavigation(appComponent)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(appComponent: AppComponent) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Connection.route) {
        composable(Screen.Connection.route) {
            ConnectionScreen(
                appComponent
                // onConnectionSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Connection.route) { inclusive = true } } }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen({}, {})
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
