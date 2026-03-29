@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.eswarm.mahati.home

import PermissionState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.eswarm.mahati.AppComponent
import `in`.eswarm.mahati.db.MqttConnectionModel
import `in`.eswarm.mahati.mqtt.common.MqttClientState
import `in`.eswarm.mahati.resources.Res
import `in`.eswarm.mahati.resources.permission_denied_notification
import `in`.eswarm.mahati.resources.permission_grant
import `in`.eswarm.mahati.resources.permission_rationale
import `in`.eswarm.mahati.util.isAndroid
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    onNavigateToNewConnection: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToConnectionDetails: (clientID: String) -> Unit,
    onEditConnection: (clientID: String) -> Unit,
    onNavigateToScanQr: () -> Unit, // Added this
    onNavigateToLog: () -> Unit,
    appComponent: AppComponent,
    permissionState: PermissionState?,
    permissionRationale: () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Mahati : MQTT Client") },
            actions = {
                if (isAndroid() && false) {
                    IconButton(onClick = { onNavigateToScanQr() }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR Code")
                    }
                }
                IconButton(onClick = { onNavigateToLog() }) {
                    Icon(Icons.Default.Article, contentDescription = "View Logs")
                }
                IconButton(onClick = { onNavigateToSettings() }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        )
    }, floatingActionButton = {
        // FAB is only for compact (mobile) layout
        FloatingActionButton(onClick = onNavigateToNewConnection) {
            Icon(Icons.Filled.Add, contentDescription = "Add new MQTT connection")
        }
    }) { innerPadding ->
        ConnectionListContent(
            modifier = Modifier.padding(innerPadding),
            appComponent = appComponent,
            onNavigateToNewConnection = onNavigateToNewConnection,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToConnectionDetails = onNavigateToConnectionDetails,
            onEditConnection = onEditConnection,
            permissionState = permissionState,
            permissionRationale = permissionRationale
        )
    }
}

@Composable
fun ConnectionListContent(
    modifier: Modifier = Modifier,
    appComponent: AppComponent,
    onNavigateToNewConnection: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToConnectionDetails: (clientID: String) -> Unit,
    onEditConnection: (clientID: String) -> Unit,
    permissionState: PermissionState? = null,
    permissionRationale: () -> Unit = {},
    onConnectionsUpdated: (isEmpty: Boolean) -> Unit = {}
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(
            appComponent.connectionRepo, appComponent.mqttController
        )
    )
    val profiles by viewModel.profiles.collectAsState(emptyList())
    val sideEffect by viewModel.sideEffects.collectAsState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val connectionStates by viewModel.mqttConnectionStates.collectAsStateWithLifecycle()

    LaunchedEffect(profiles) {
        onConnectionsUpdated(profiles.isEmpty())
    }

    LaunchedEffect(sideEffect) {
        when (val effect = sideEffect) {
            is HomeSideEffect.NavigateToNewConnectionScreen -> onNavigateToNewConnection()
            is HomeSideEffect.NavigateToConnectionDetails -> onNavigateToConnectionDetails(effect.clientID)
            is HomeSideEffect.DeleteConnection -> viewModel.deleteConnection(effect.clientID)
            is HomeSideEffect.EditConnection -> onEditConnection(effect.clientID)
            HomeSideEffect.NavigateToSettingsScreen -> onNavigateToSettings()
            null -> { /* No-op */
            }
        }
        viewModel.clearSideEffect()
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isCompact = maxWidth < 600.dp

        Column(Modifier.fillMaxSize()) {
            if (permissionState != PermissionState.GRANTED) {
                PermissionView(permissionState, permissionRationale)
            }

            if (profiles.isEmpty()) {
                val text = if (isCompact)
                    "No MQTT connections yet. \nTap the '+' button to add one."
                else
                    "No connections"
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                ConnectionsList(
                    profiles = profiles,
                    connectionStates = connectionStates,
                    onProfileClick = { profileId ->
                        viewModel.onEvent(HomeUiEvent.ConnectionSelected(profileId))
                    },
                    onDeleteAction = { profileId ->
                        viewModel.onEvent(HomeUiEvent.DeleteConnectionClicked(profileId))
                    },
                    onEditAction = { profileId ->
                        viewModel.onEvent(HomeUiEvent.EditConnectionClicked(profileId))
                    },
                    modifier = Modifier.weight(1f)
                )

                if (!isCompact) {
                    Button(
                        onClick = { viewModel.onEvent(HomeUiEvent.AddNewConnectionClicked) },
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("New Connection")
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionView(permissionState: PermissionState?, permissionRationale: () -> Unit) {
    if (permissionState == null) return
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (permissionState) {
            PermissionState.DENIED -> Text(stringResource(Res.string.permission_denied_notification))
            PermissionState.SHOW_RATIONALE -> {
                Text(stringResource(Res.string.permission_rationale), textAlign = TextAlign.Center)
                Button(onClick = { permissionRationale() }) {
                    Text(stringResource(Res.string.permission_grant))
                }
            }
            else -> {}
        }
    }
}

@Composable
fun ConnectionsList(
    profiles: List<MqttConnectionModel>,
    connectionStates: Map<String, MqttClientState>,
    onProfileClick: (String) -> Unit,
    onDeleteAction: (String) -> Unit,
    onEditAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(profiles, key = { it.id }) { connectionParamsEntity ->
            ConnectionListItem(
                connectionDetails = connectionParamsEntity,
                connectionState = connectionStates[connectionParamsEntity.clientID],
                clickAction = { onProfileClick(connectionParamsEntity.clientID) },
                deleteAction = { clientID -> onDeleteAction(clientID) },
                editAction = { clientID -> onEditAction(clientID) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionListItem(
    connectionDetails: MqttConnectionModel,
    connectionState: MqttClientState?,
    clickAction: () -> Unit,
    deleteAction: (clientID: String) -> Unit,
    editAction: (clientID: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnecting = connectionState is MqttClientState.Connecting
    Card(
        onClick = if (isConnecting) { {}
        } else clickAction, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val statusColor = when (connectionState) {
                is MqttClientState.Connected -> Color.Green
                is MqttClientState.Connecting -> Color.Yellow
                is MqttClientState.Disconnected -> Color.Gray
                is MqttClientState.Error -> Color.Red
                else -> Color.Gray
            }
            Box(
                modifier = Modifier.size(12.dp).clip(CircleShape).background(statusColor)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = connectionDetails.clientID,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${connectionDetails.brokerHost}:${connectionDetails.brokerPort}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp), // Standard icon size
                    color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp
                )
            } else {
                Row {
                    IconButton(onClick = { editAction(connectionDetails.clientID) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit connection",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { deleteAction(connectionDetails.clientID) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete connection",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
