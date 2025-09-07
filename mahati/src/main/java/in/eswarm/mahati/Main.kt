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
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation() // Call the main navigation composable
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Connection.route) {
        composable(Screen.Connection.route) {
            ConnectionScreen(
                // Assuming ConnectionViewModel is injected or provided by viewModel()
                // If ConnectionScreen needs to navigate, pass navController:
                // onConnectionSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Connection.route) { inclusive = true } } }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSubscription = { navController.navigate(Screen.TopicSubscription.route) },
                onNavigateToChat = { topic -> navController.navigate(Screen.Chat.createRoute(topic)) }
            )
        }
        composable(Screen.TopicSubscription.route) {
            TopicSubscriptionScreen(
                // Assuming ViewModel is injected or provided
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
                    topicName = topicName
                    // Assuming ViewModel is injected or provided
                    // onNavigateBack = { navController.popBackStack() }
                )
            } else {
                // Handle error: topicName not found, perhaps navigate back or show error
            }
        }
    }
}
