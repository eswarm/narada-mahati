@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.eswarm.mahati.home

import PermissionState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import `in`.eswarm.mahati.db.MqttConnection
import `in`.eswarm.mahati.mqtt.common.MqttClientState
import `in`.eswarm.mahati.resources.Res
import `in`.eswarm.mahati.resources.permission_denied_notification
import `in`.eswarm.mahati.resources.permission_grant
import `in`.eswarm.mahati.resources.permission_rationale
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToNewConnection: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToConnectionDetails: (clientID: String) -> Unit,
    onEditConnection: (clientID: String) -> Unit,
    appComponent: AppComponent,
    permissionState: PermissionState,
    permissionRationale: () -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(
            appComponent.connectionRepo, appComponent.mqttController
        )
    )
) {
    val profiles by viewModel.profiles.collectAsState(emptyList())
    val sideEffect by viewModel.sideEffects.collectAsState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val connectionStates by viewModel.mqttConnectionStates.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.connectionError) {
        uiState.connectionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearConnectionError()
        }
    }

    LaunchedEffect(sideEffect) {
        when (val effect = sideEffect) {
            is HomeSideEffect.NavigateToNewConnectionScreen -> {
                onNavigateToNewConnection()
            }

            is HomeSideEffect.NavigateToConnectionDetails -> {
                onNavigateToConnectionDetails(effect.clientID)
            }

            is HomeSideEffect.DeleteConnection -> {
                viewModel.deleteConnection(effect.clientID)
            }

            is HomeSideEffect.EditConnection -> {
                onEditConnection(effect.clientID)
            }

            HomeSideEffect.NavigateToSettingsScreen -> {
                onNavigateToSettings()
            }

            null -> { /* No-op */

            }
        }
        viewModel.clearSideEffect()
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        TopAppBar(
            title = { Text("Mahati : MQTT Client") })
    }, floatingActionButton = {
        FloatingActionButton(onClick = { viewModel.onEvent(HomeUiEvent.AddNewConnectionClicked) }) {
            Icon(Icons.Filled.Add, contentDescription = "Add new MQTT connection")
        }
    }) { innerPadding ->

        Column(Modifier.padding(innerPadding).fillMaxSize()) {

            if (permissionState != PermissionState.GRANTED) {
                PermissionView(permissionState, permissionRationale)
            }

            if (profiles.isEmpty()) {
                EmptyConnectionsView(modifier = Modifier.weight(1f))
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
            }
        }
    }
}

@Composable
fun PermissionView(permissionState: PermissionState, permissionRationale: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (permissionState == PermissionState.DENIED) {
            Text(
                stringResource(Res.string.permission_denied_notification)
            )
        } else if (permissionState == PermissionState.SHOW_RATIONALE) {
            Text(
                stringResource(Res.string.permission_rationale),
                textAlign = TextAlign.Center
            )
            Button(onClick = { permissionRationale() }) {
                Text(stringResource(Res.string.permission_grant))
            }
        }
    }
}

@Composable
fun ConnectionsList(
    profiles: List<MqttConnection>,
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
                deleteAction = { clientID ->
                    onDeleteAction(clientID)
                },
                editAction = { clientID -> onEditAction(clientID) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionListItem(
    connectionDetails: MqttConnection,
    connectionState: MqttClientState?,
    clickAction: () -> Unit,
    deleteAction: (clientID: String) -> Unit,
    editAction: (clientID: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnecting = connectionState is MqttClientState.Connecting
    Card(
        onClick = if (isConnecting) {
            {}
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
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(statusColor)
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

                    IconButton(onClick = {
                        editAction(connectionDetails.clientID)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit connection",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            deleteAction(connectionDetails.clientID)
                        }) {
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

@Composable
fun EmptyConnectionsView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(vertical = 64.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No MQTT connections yet. \nTap the '+' button to add one.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
