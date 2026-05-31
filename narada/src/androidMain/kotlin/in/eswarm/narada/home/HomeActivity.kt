package `in`.eswarm.narada.home

import android.annotation.SuppressLint
import `in`.eswarm.narada.util.NotificationUtil
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import `in`.eswarm.narada.App
import `in`.eswarm.narada.util.getAppComponent
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class HomeActivity : ComponentActivity() {

    private val mainScope = MainScope()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appComponent = getAppComponent()
        val preferences = appComponent.appPreferences
        NotificationUtil.createNotificationChannel(this)
        mainScope.launch {
            preferences.setPassword()
        }

        setContent {
            App(appComponent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }
}
