package `in`.eswarm.mahati.topics.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.eswarm.mahati.mqtt.core.MqttManager
import `in`.eswarm.mahati.topics.model.SubscribedTopic
import `in`.eswarm.mahati.topics.viewmodel.TopicSubscriptionEvent
import `in`.eswarm.mahati.topics.viewmodel.TopicSubscriptionUiState
import `in`.eswarm.mahati.topics.viewmodel.TopicSubscriptionViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CompletableDeferred

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicSubscriptionScreen(
    // You'll need a ViewModelFactory or DI to provide the MqttManager
    viewModel: TopicSubscriptionViewModel // = viewModel(factory = YourViewModelFactory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError() // Clear error after showing
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Subscribed MQTT Topics") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.onEvent(TopicSubscriptionEvent.FabClicked) }) {
                Icon(Icons.Filled.Add, contentDescription = "Subscribe to new topic")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (uiState.subscribedTopics.isEmpty() && !uiState.isLoading) {
                EmptySubscriptionView(
                    onSubscribeClick = { viewModel.onEvent(TopicSubscriptionEvent.FabClicked) },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                SubscribedTopicsList(
                    topics = uiState.subscribedTopics,
                    onUnsubscribe = { topicFilter ->
                        viewModel.onEvent(TopicSubscriptionEvent.UnsubscribeFromTopic(topicFilter))
                    }
                )
            }

            if (uiState.isLoading && uiState.subscribedTopics.isEmpty()) { // Show full screen loader if loading initial list
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if (uiState.showSubscribeDialog) {
                SubscribeToTopicDialog(
                    onDismiss = { viewModel.onEvent(TopicSubscriptionEvent.DismissSubscribeDialog) },
                    onConfirm = { topicFilter, qos ->
                        viewModel.onEvent(TopicSubscriptionEvent.SubscribeToTopic(topicFilter, qos))
                    }
                )
            }
        }
    }
}

@Composable
fun SubscribedTopicsList(
    topics: List<SubscribedTopic>,
    onUnsubscribe: (String) -> Unit,
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
                onUnsubscribe = { onUnsubscribe(topic.topicFilter) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscribedTopicItem(
    topic: SubscribedTopic,
    onUnsubscribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
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
    onDismiss: () -> Unit,
    onConfirm: (topicFilter: String, qos: Int) -> Unit
) {
    var topicFilter by remember { mutableStateOf("") }
    var qosString by remember { mutableStateOf("0") } // QoS as string for TextField

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
                    label = { Text("Topic Filter (e.g., /myhome/lights)") },
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
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


// --- Preview Setup ---

// A mock MqttManager for previews
class PreviewMqttManager : MqttManager {
    override val connectionState: StateFlow<`in`.eswarm.mahati.mqtt.common.MqttClientState> = MutableStateFlow(`in`.eswarm.mahati.mqtt.common.MqttClientState.Disconnected)
    // override val receivedMessages: StateFlow<`in`.eswarm.mahati.mqtt.common.ReceivedMqttMessage?> = MutableStateFlow(null) // Adjusted for preview
     // Simplified receivedMessages for preview
    private val _previewReceivedMessages = MutableStateFlow<`in`.eswarm.mahati.mqtt.common.ReceivedMqttMessage?>(null)
    override val receivedMessages: StateFlow<`in`.eswarm.mahati.mqtt.common.ReceivedMqttMessage?> = _previewReceivedMessages.asStateFlow() // Fixed to use SharedFlow's subtyping with StateFlow for preview simplicity


    override fun connect(params: `in`.eswarm.mahati.mqtt.common.MqttConnectionParams) {}
    override fun disconnect() {}
    override suspend fun publish(topic: String, message: String, qos: Int, retain: Boolean): Boolean = true
    override suspend fun publish(topic: String, payload: ByteArray, qos: Int, retain: Boolean): Boolean = true
    override suspend fun subscribe(topicFilter: String, qos: Int): Boolean = CompletableDeferred<Boolean>().await() // Simulates suspension
    override suspend fun unsubscribe(topicFilter: String): Boolean = true
    override fun cleanup() {}
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showBackground = true, name = "Topic Subscription Screen - Empty")
@Composable
fun TopicSubscriptionScreenPreview_Empty() {
    val previewMqttManager = PreviewMqttManager()
    val viewModel = TopicSubscriptionViewModel(previewMqttManager)
    MaterialTheme {
        TopicSubscriptionScreen(viewModel = viewModel)
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showBackground = true, name = "Topic Subscription Screen - With Items")
@Composable
fun TopicSubscriptionScreenPreview_WithItems() {
    val previewMqttManager = PreviewMqttManager()
    val viewModel = TopicSubscriptionViewModel(previewMqttManager).apply {
        // Manually set state for preview
        val previewTopics = listOf(
            SubscribedTopic("home/livingroom/temp", 0, Date(System.currentTimeMillis() - 100000)),
            SubscribedTopic("sensors/+/data", 1, Date(System.currentTimeMillis() - 200000)),
            SubscribedTopic("alerts/#", 2)
        )
        // This direct update is for preview simplicity. In real code, go through events.
         (this.uiState as MutableStateFlow).value = TopicSubscriptionUiState(subscribedTopics = previewTopics)
    }
    MaterialTheme {
        TopicSubscriptionScreen(viewModel = viewModel)
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showBackground = true, name = "Topic Subscription Screen - Dialog Open")
@Composable
fun TopicSubscriptionScreenPreview_Dialog() {
    val previewMqttManager = PreviewMqttManager().apply {
        // It seems the MqttManager interface's receivedMessages is SharedFlow, not StateFlow.
        // For preview, we often simplify. If it must be SharedFlow, PreviewMqttManager needs to reflect that.
        // However, the ViewModel currently expects StateFlow<ReceivedMqttMessage?> which is a bit unusual for a stream of messages.
        // The MqttManagerImpl uses MutableSharedFlow for receivedMessages.
        // Let's assume for Preview we can provide a compatible StateFlow for simplicity.
    }
    val viewModel = TopicSubscriptionViewModel(previewMqttManager).apply {
         (this.uiState as MutableStateFlow).value = TopicSubscriptionUiState(showSubscribeDialog = true)
    }
    MaterialTheme {
        TopicSubscriptionScreen(viewModel = viewModel)
    }
}


