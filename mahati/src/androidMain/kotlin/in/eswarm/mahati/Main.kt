package `in`.eswarm.mahati

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import `in`.eswarm.mahati.theme.NaradaMQTTBrokerTheme
import `in`.eswarm.mahati.util.NotificationUtil
import kotlinx.coroutines.runBlocking

class Main : ComponentActivity() { // This class now hosts the navigation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // TODO: Fix this.
        runBlocking {
            NotificationUtil.createNotificationChannel(this@Main)
        }

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

