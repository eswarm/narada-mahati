@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.eswarm.mahati.home

import PermissionState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.eswarm.mahati.AppComponent
import `in`.eswarm.mahati.connection.ConnectionUiState
import `in`.eswarm.mahati.db.MqttConnection
import `in`.eswarm.mahati.resources.Res
import `in`.eswarm.mahati.resources.permission_denied_notification
import `in`.eswarm.mahati.resources.permission_grant
import `in`.eswarm.mahati.resources.permission_rationale
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToNewConnection: () -> Unit,
    onNavigateToConnectionDetails: (clientID: String) -> Unit,
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

    LaunchedEffect(sideEffect) {
        when (val effect = sideEffect) {
            is HomeSideEffect.NavigateToNewConnectionScreen -> {
                onNavigateToNewConnection()
                viewModel.clearSideEffect()
            }

            is HomeSideEffect.NavigateToConnectionDetails -> {
                onNavigateToConnectionDetails(effect.clientID)
                viewModel.clearSideEffect()
            }

            null -> { /* No-op */
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Mahati : MQTT CLient") })
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
                    uiState = uiState,
                    onProfileClick = { profileId ->
                        viewModel.onEvent(HomeUiEvent.ConnectionSelected(profileId))
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
    uiState: ConnectionUiState,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(profiles, key = { it.id }) { connectionParamsEntity ->
            ConnectionListItem(
                paramsEntity = connectionParamsEntity,
                // Calculate and pass isConnecting for this specific item
                isConnecting = uiState.isConnecting && uiState.connectingClientId == connectionParamsEntity.clientID,
                onClick = { onProfileClick(connectionParamsEntity.clientID) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionListItem(
    paramsEntity: MqttConnection,
    isConnecting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = if (isConnecting) {
            {}
        } else onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = paramsEntity.clientID,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${paramsEntity.brokerHost}:${paramsEntity.brokerPort}",
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
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Connection status or type",
                    tint = MaterialTheme.colorScheme.primary
                )
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
