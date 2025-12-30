package `in`.eswarm.narada.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import `in`.eswarm.narada.log.LogView
import `in`.eswarm.narada.resources.Res
import `in`.eswarm.narada.resources.app_name
import `in`.eswarm.narada.resources.clients_connected
import `in`.eswarm.narada.resources.ip_address
import `in`.eswarm.narada.resources.logs
import `in`.eswarm.narada.resources.no_notification_permission_description
import `in`.eswarm.narada.resources.request_permission
import `in`.eswarm.narada.resources.settings
import `in`.eswarm.narada.resources.start_server
import `in`.eswarm.narada.resources.stop_server
import `in`.eswarm.narada.share.ShareQrCodeDialog
import `in`.eswarm.narada.util.PlatformUtil
import `in`.eswarm.narada.util.isAndroid
import `in`.eswarm.narada.util.postNotificationPermission
import `in`.eswarm.narada.util.rememberPermissionState
import org.jetbrains.compose.resources.stringResource

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    navController: NavController
) {
    val isServerRunning = homeViewModel.isServerRunning.collectAsState()
    val notifPermissionState = rememberPermissionState(postNotificationPermission)
    val showQrDialog = rememberSaveable { mutableStateOf(false) }

    if (showQrDialog.value) {
        val connectionString = homeViewModel.getConnectionString()
        if (connectionString != null) {
            ShareQrCodeDialog(
                onDismissRequest = { showQrDialog.value = false },
                connectionDetailsJson = connectionString
            )
        }
    }

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = { navController.navigate("settings") }) {
            Icon(
                Icons.Filled.Settings,
                contentDescription = stringResource(Res.string.settings),
            )
        }
    }, topBar = {
        TopAppBar(
            title = { Text(stringResource(Res.string.app_name)) }, actions = {
                if (isAndroid() && false) { // Disable for now.
                    IconButton(onClick = { showQrDialog.value = true }) {
                        Icon(
                            Icons.Filled.QrCode, contentDescription = "Share Credentials"
                        )
                    }
                }
            }, modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(horizontal = 16.dp)
        ) {

            Row(modifier = Modifier.padding(vertical = Dp(4f))) {
                Text(
                    "State", style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    if (isServerRunning.value) {
                        "Running"
                    } else {
                        "Stopped"
                    }, lineHeight = TextUnit(24f, TextUnitType.Sp)
                )
            }

            Row(modifier = Modifier.padding(vertical = Dp(4f))) {
                Text(
                    stringResource(Res.string.ip_address),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(homeViewModel.getLocalIpAddress() ?: "-")
            }

            Row(modifier = Modifier.padding(vertical = Dp(4f))) {
                Text(
                    stringResource(Res.string.clients_connected),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(homeViewModel.clientsCount.value.toString())
            }

            Row(modifier = Modifier.padding(vertical = Dp(16f))) {
                Button(
                    onClick = {
                        homeViewModel.toggleServer()
                    }, modifier = Modifier.padding(horizontal = Dp(16f)).weight(1f)
                ) {
                    val buttonText = if (isServerRunning.value) {
                        stringResource(Res.string.stop_server)
                    } else {
                        stringResource(Res.string.start_server)
                    }
                    Text(buttonText)
                }
            }

            Row(
                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(Res.string.logs),
                    style = MaterialTheme.typography.headlineSmall,
                )

                IconButton(
                    content = { Icon(Icons.Filled.Delete, "Delete") },
                    onClick = { homeViewModel.clearLogs() })
            }

            if (!PlatformUtil.isNotificationPermissionRequired || notifPermissionState.status.isGranted) {
                LogView(homeViewModel.logs)
            } else {
                Column(modifier = Modifier.padding(vertical = Dp(16f))) {
                    Text(text = stringResource(Res.string.no_notification_permission_description))
                    Button(
                        modifier = Modifier.padding(all = 16.dp)
                            .align(Alignment.CenterHorizontally),
                        onClick = { notifPermissionState.launchPermissionRequest() }) {
                        Text(text = stringResource(Res.string.request_permission))
                    }
                }
            }
        }
    }
}
