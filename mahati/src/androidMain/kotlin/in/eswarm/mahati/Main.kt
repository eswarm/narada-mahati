package `in`.eswarm.mahati

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import `in`.eswarm.mahati.theme.NaradaMQTTBrokerTheme
import `in`.eswarm.mahati.util.NotificationUtil
import kotlinx.coroutines.runBlocking

class Main : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

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
            NotificationUtil.createNotificationChannel(this@Main)
        }

        setContent {
            NaradaMQTTBrokerTheme {
                val permissionState by viewModel.permissionState.collectAsState()
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
                            requestPermission = ::requestPermission
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
}
