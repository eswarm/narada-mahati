package `in`.eswarm.mahati.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.eswarm.mahati.AppComponent
import `in`.eswarm.mahati.db.MessageRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    appComponent: AppComponent,
    clientID: String,
    topic: String,
    messageRepo: MessageRepository,
    viewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.Factory(
            appComponent.mqttController,
            clientID,
            topic,
            messageRepo
        )
    )
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(connectionState) {
        val connectionState = connectionState[clientID] ?: return@LaunchedEffect
        viewModel.onMqttStateChange(connectionState)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Topic: ${viewModel.topic}") }, 
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                currentText = uiState.currentInput,
                onTextChanged = { viewModel.onInputChange(it) },
                onSendClicked = { viewModel.sendMessage() },
                isConnected = uiState.isConnected
            )
        },
        contentWindowInsets = WindowInsets.ime // <-- MODIFIED: Scaffold handles IME insets
    ) { innerPadding -> // innerPadding now includes IME adjustments
        if (!uiState.isConnected && uiState.messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Connecting to chat...", style = MaterialTheme.typography.titleMedium)
            }
        } else if (uiState.messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No messages yet. Send one!", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // Apply Scaffold's adjusted padding
                    .imePadding()       // <-- MODIFIED: Removed explicit IME padding here
                    .padding(horizontal = 8.dp), // Then other content padding
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    AnimatedVisibility(
                        visible = true, // Messages are always visible once added
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                    ) {
                        MessageBubble(message = message)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val bubbleColor =
        if (message.isSentByUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor =
        if (message.isSentByUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    val alignment = if (message.isSentByUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleShape = if (message.isSentByUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }
    val simpleDateFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxWidth()) { // Occupy full width to use alignment
        Column(
            modifier = Modifier
                .align(alignment)
                .widthIn(max = 300.dp) // Max width for a bubble
                .clip(bubbleShape)
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (!message.isSentByUser) { // Show sender ID for received messages if desired (e.g. group chat)
                Text(
                    text = message.senderId, // Or a display name if available
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            Text(
                text = message.text,
                color = textColor,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = simpleDateFormat.format(message.timestamp),
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
                if (message.isSentByUser && message.status != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    MessageStatusIcon(status = message.status)
                }
            }
        }
    }
}

@Composable
fun MessageStatusIcon(status: MessageStatus) {
    val icon: ImageVector
    val tint: Color
    when (status) {
        MessageStatus.SENDING -> {
            icon = Icons.Default.DateRange
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        }

        MessageStatus.SENT -> {
            icon = Icons.Default.Check // Single check for sent to broker
            tint = MaterialTheme.colorScheme.primary // Or a specific "sent" color
        }

        MessageStatus.FAILED -> {
            icon = Icons.Default.Close
            tint = MaterialTheme.colorScheme.error
        }
    }
    Icon(
        imageVector = icon,
        contentDescription = "Status: $status",
        tint = tint,
        modifier = Modifier.size(14.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputBar(
    currentText: String,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    isConnected: Boolean
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(
        modifier = Modifier.navigationBarsPadding(),
        tonalElevation = 3.dp, // Adds a slight shadow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = currentText,
                onValueChange = onTextChanged,
                modifier = Modifier.weight(1f),
                placeholder = { Text(if (isConnected) "Type a message..." else "Connecting...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (currentText.isNotBlank() && isConnected) {
                        onSendClicked()
                        // keyboardController?.hide() // Optionally hide keyboard
                    }
                }),
                enabled = isConnected
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (currentText.isNotBlank()) {
                        onSendClicked()
                        // keyboardController?.hide() // Optionally hide keyboard
                    }
                },
                enabled = currentText.isNotBlank() && isConnected,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send message")
            }
        }
    }
}

/*
// --- Preview Section ---
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showBackground = true, name = "Chat Screen - Empty")
@Composable
fun ChatScreenPreview_Empty() {
    val previewMqttManager = PreviewMqttManager() // Assuming PreviewMqttManager is accessible
    val viewModel = ChatViewModel(previewMqttManager, "preview/chat", "user123")
    (viewModel.uiState as MutableStateFlow).value =
        ChatUiState(isConnected = true, messages = emptyList())
    MaterialTheme {
        ChatScreen(viewModel = viewModel)
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showBackground = true, name = "Chat Screen - With Messages")
@Composable
fun ChatScreenPreview_WithMessages() {
    val previewMqttManager = PreviewMqttManager()
    val viewModel = ChatViewModel(previewMqttManager, "preview/chat", "user123")

    val previewMessages = listOf(
        ChatMessage(
            text = "Hello there!",
            senderId = "userOther",
            isSentByUser = false,
            timestamp = Date(System.currentTimeMillis() - 200000)
        ),
        ChatMessage(
            text = "Hi! How are you?",
            senderId = "user123",
            isSentByUser = true,
            status = MessageStatus.SENT,
            timestamp = Date(System.currentTimeMillis() - 100000)
        ),
        ChatMessage(
            text = "Doing great! This chat UI looks neat.",
            senderId = "userOther",
            isSentByUser = false,
            timestamp = Date(System.currentTimeMillis() - 50000)
        ),
        ChatMessage(
            text = "Thanks! Trying to make it WhatsApp-like.",
            senderId = "user123",
            isSentByUser = true,
            status = MessageStatus.SENDING
        )
    )
    (viewModel.uiState as MutableStateFlow).value =
        ChatUiState(isConnected = true, messages = previewMessages)

    MaterialTheme {
        ChatScreen(viewModel = viewModel)
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showBackground = true, name = "Chat Screen - Not Connected")
@Composable
fun ChatScreenPreview_NotConnected() {
    val previewMqttManager = PreviewMqttManager()
    val viewModel = ChatViewModel(previewMqttManager, "preview/chat", "user123")
    (viewModel.uiState as MutableStateFlow).value =
        ChatUiState(isConnected = false, messages = emptyList())
    MaterialTheme {
        ChatScreen(viewModel = viewModel)
    }
}

*/