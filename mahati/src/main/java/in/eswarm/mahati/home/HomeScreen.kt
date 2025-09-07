@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.eswarm.mahati.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.eswarm.mahati.mqtt.common.MqttConnectionParams // Required for Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToNewConnection: () -> Unit,
    onNavigateToConnectionDetails: (profileId: String) -> Unit,
    viewModel: HomeViewModel = viewModel(),
    ) {
    val profiles by viewModel.profiles.collectAsState()
    val sideEffect by viewModel.sideEffects.collectAsState()

    LaunchedEffect(sideEffect) {
        when (val effect = sideEffect) {
            is HomeSideEffect.NavigateToNewConnectionScreen -> {
                onNavigateToNewConnection()
                viewModel.clearSideEffect()
            }
            is HomeSideEffect.NavigateToConnectionDetails -> {
                onNavigateToConnectionDetails(effect.profileId)
                viewModel.clearSideEffect()
            }
            null -> { /* No-op */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mahati MQTT Connections") } 
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.onEvent(HomeUiEvent.AddNewConnectionClicked) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add new MQTT connection")
            }
        }
    ) { innerPadding ->
        if (profiles.isEmpty()) {
            EmptyConnectionsView(modifier = Modifier.padding(innerPadding))
        } else {
            ConnectionsList(
                profiles = profiles,
                onProfileClick = { profileId ->
                    viewModel.onEvent(HomeUiEvent.ConnectionSelected(profileId))
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun ConnectionsList(
    profiles: List<MqttConnectionProfile>,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(profiles, key = { it.id }) { profile ->
            ConnectionListItem(
                profile = profile,
                onClick = { onProfileClick(profile.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionListItem(
    profile: MqttConnectionProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${profile.params.brokerHost}:${profile.params.brokerPort}",
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
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No MQTT connections yet.\nTap the '+' button to add one.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showBackground = true, name = "Home Screen With Items")
@Composable
fun HomeScreenPreview_WithItems() {
    val previewProfiles = listOf(
        MqttConnectionProfile("1", "Mahati Local", MqttConnectionParams(brokerHost = "192.168.1.5", brokerPort = 1883, clientId = "client1")),
        MqttConnectionProfile("2", "Mahati Cloud", MqttConnectionParams(brokerHost = "cloud.mqtt.com", brokerPort = 1883, clientId = "client2"))
    )
    MaterialTheme { 
        Scaffold(
             topBar = { TopAppBar(title = { Text("MQTT Connections Preview") }) },
             floatingActionButton = { FloatingActionButton(onClick = {}) { Icon(Icons.Filled.Add, "") } }
        ) {
            ConnectionsList(profiles = previewProfiles, onProfileClick = {}, modifier = Modifier.padding(it))
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showBackground = true, name = "Home Screen Empty")
@Composable
fun HomeScreenPreview_Empty() {
     MaterialTheme {
        Scaffold(
             topBar = { TopAppBar(title = { Text("MQTT Connections Preview") }) },
             floatingActionButton = { FloatingActionButton(onClick = {}) { Icon(Icons.Filled.Add, "") } }
        ) {
            EmptyConnectionsView(modifier = Modifier.padding(it))
        }
    }
}
