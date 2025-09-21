@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.eswarm.mahati.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Card
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
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.eswarm.mahati.AppComponent
import `in`.eswarm.mahati.db.MqttConnection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToNewConnection: () -> Unit,
    onNavigateToConnectionDetails: (clientID: String) -> Unit,
    appComponent: AppComponent,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(
            appComponent.connectionRepo,
            appComponent.mqttManager
        )
    ),
) {
    val profiles by viewModel.profiles.collectAsState(emptyList())
    val sideEffect by viewModel.sideEffects.collectAsState()

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
        if (profiles.isEmpty()) {
            EmptyConnectionsView(modifier = Modifier.padding(innerPadding))
        } else {
            ConnectionsList(
                profiles = profiles, onProfileClick = { profileId ->
                    viewModel.onEvent(HomeUiEvent.ConnectionSelected(profileId))
                }, modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun ConnectionsList(
    profiles: List<MqttConnection>,
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
                onClick = { onProfileClick(connectionParamsEntity.clientID) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionListItem(
    paramsEntity: MqttConnection, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick, modifier = modifier.fillMaxWidth()
    ) {
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
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = "Connection status or type",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EmptyConnectionsView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No MQTT connections yet.\nTap the '+' button to add one.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

