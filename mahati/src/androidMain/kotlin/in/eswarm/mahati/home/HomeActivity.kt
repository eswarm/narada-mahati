package `in`.eswarm.mahati.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import `in`.eswarm.mahati.AppNavigation
import `in`.eswarm.mahati.MahatiApplication
import `in`.eswarm.mahati.navigation.DeepLinkDestination
import `in`.eswarm.mahati.theme.NaradaMQTTBrokerTheme
import `in`.eswarm.mahati.util.NotificationUtil
import kotlinx.coroutines.runBlocking

class HomeActivity : ComponentActivity() {
    private val viewModel: HomeDroidViewModel by viewModels()
    private var deepLinkDestinationState = mutableStateOf<DeepLinkDestination?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.onPermissionGranted()
        } else {
            // The user denied the permission. The new state is either RATIONALE or DENIED.
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                viewModel.onPermissionRationale()
            } else {
                viewModel.onPermissionDenied()
            }
        }
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.onPermissionGranted()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) -> {
                viewModel.onPermissionRationale()
            }

            else -> {
                requestPermission()
                viewModel.onPermissionDenied()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // TODO: Fix this.
        runBlocking {
            NotificationUtil.createNotificationChannel(this@HomeActivity)
            NotificationUtil.createMessageChannel(this@HomeActivity)
        }

        deepLinkDestinationState.value = parseDeepLink(intent)

        setContent {
            NaradaMQTTBrokerTheme {
                val permissionState by viewModel.permissionState.collectAsState()
                var deepLinkDestination by deepLinkDestinationState

                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    // Only display the navigation once the permission state has been determined.
                    permissionState?.let { state ->
                        val appComponent =
                            (this.applicationContext as MahatiApplication).appComponent
                        AppNavigation(
                            appComponent = appComponent,
                            permissionState = state,
                            requestPermission = ::requestPermission,
                            deepLinkDestination = deepLinkDestination,
                            onDeepLinkHandled = { deepLinkDestination = null }
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkPermission()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val deepLink = parseDeepLink(intent)
        if (deepLink != null) {
            deepLinkDestinationState.value = deepLink
        }
    }

    private fun parseDeepLink(intent: Intent?): DeepLinkDestination? {
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            val uri: Uri = intent.data!!
            if (uri.scheme == "mahati" && uri.host == "chat") {
                val clientId = uri.pathSegments.getOrNull(0)
                val topicName = uri.pathSegments.getOrNull(1)
                if (clientId != null && topicName != null) {
                    return DeepLinkDestination.Chat(clientId, topicName)
                }
            }
        }
        return null
    }
}
