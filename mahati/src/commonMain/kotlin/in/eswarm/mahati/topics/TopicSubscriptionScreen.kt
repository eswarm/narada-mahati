package `in`.eswarm.mahati.topics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import `in`.eswarm.mahati.AppComponent
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.eswarm.mahati.db.SubscribedTopic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicSubscriptionScreen(
    appComponent: AppComponent,
    clientID: String,
    viewModel: TopicSubscriptionViewModel = viewModel(
        factory = TopicViewModelFactory(
            appComponent.mqttController,
            clientID,
            appComponent.subscriptionRepo
        )
    ),
    onTopicClick: (topic: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(
                message = it, duration = SnackbarDuration.Short
            )
            viewModel.clearError() // Clear error after showing
        }
    }

    LaunchedEffect(clientID) {
        viewModel.load()
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        TopAppBar(title = { Text("Subscribed MQTT Topics") })
    }, floatingActionButton = {
        FloatingActionButton(onClick = { viewModel.onEvent(TopicSubscriptionEvent.FabClicked) }) {
            Icon(Icons.Filled.Add, contentDescription = "Subscribe to new topic")
        }
    }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (uiState.subscribedTopics.isEmpty() && !uiState.isLoading) {
                EmptySubscriptionView(
                    onSubscribeClick = { viewModel.onEvent(TopicSubscriptionEvent.FabClicked) },
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
            } else {
                SubscribedTopicsList(
                    topics = uiState.subscribedTopics, onUnsubscribe = { topicFilter ->
                        viewModel.onEvent(TopicSubscriptionEvent.UnsubscribeFromTopic(topicFilter))
                    }, onTopicClick = onTopicClick
                )
            }

            if (uiState.isLoading && uiState.subscribedTopics.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if (uiState.showSubscribeDialog) {
                SubscribeToTopicDialog(
                    onDismiss = { viewModel.onEvent(TopicSubscriptionEvent.DismissSubscribeDialog) },
                    onConfirm = { topicFilter, qos ->
                        viewModel.onEvent(TopicSubscriptionEvent.SubscribeToTopic(topicFilter, qos))
                    })
            }
        }
    }
}

@Composable
fun SubscribedTopicsList(
    topics: List<SubscribedTopic>,
    onUnsubscribe: (String) -> Unit,
    onTopicClick: (topic: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(topics, key = { it.topicFilter }) { topic ->
            SubscribedTopicItem(
                topic = topic,
                onUnsubscribe = { onUnsubscribe(topic.topicFilter) },
                onTopicClick = onTopicClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscribedTopicItem(
    topic: SubscribedTopic,
    onUnsubscribe: () -> Unit,
    onTopicClick: (topic: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    Card(
        onClick = { onTopicClick(topic.topicFilter) }, modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Subscribed",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(topic.topicFilter, style = MaterialTheme.typography.titleMedium)
                Text(
                    "QoS: ${topic.qos} - Since: ${dateFormatter.format(topic.subscribedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onUnsubscribe) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Unsubscribe from ${topic.topicFilter}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptySubscriptionView(onSubscribeClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "No topics subscribed yet.",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSubscribeClick) {
            Text("Subscribe to a Topic")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscribeToTopicDialog(
    onDismiss: () -> Unit, onConfirm: (topicFilter: String, qos: Int) -> Unit
) {
    var topicFilter by remember { mutableStateOf("") }
    var qosString by remember { mutableStateOf("1") } // QoS as string for TextField

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Subscribe to Topic", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = topicFilter,
                    onValueChange = { topicFilter = it },
                    label = { Text("Topic e.g home/hall") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = qosString,
                    onValueChange = { qosString = it.filter { char -> char.isDigit() }.take(1) },
                    label = { Text("QoS (0, 1, or 2)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val qos = qosString.toIntOrNull() ?: 0
                        if (topicFilter.isNotBlank()) {
                            onConfirm(topicFilter, qos.coerceIn(0, 2))
                        }
                    }) {
                        Text("Subscribe")
                    }
                }
            }
        }
    }
}