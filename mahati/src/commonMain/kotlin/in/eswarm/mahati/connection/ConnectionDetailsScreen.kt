package `in`.eswarm.mahati.connection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.eswarm.mahati.AppComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionDetailsScreen(
    appComponent: AppComponent,
    onCancel: () -> Unit,
    onSuccess: () -> Unit,
    clientID: String? = null,
    connectionDetailsViewModel: ConnectionDetailsViewModel = viewModel(
        factory = ConnectionDetailsViewModel.Factory(
            appComponent.mqttController,
            appComponent.connectionRepo,
            clientID,
            onSuccess
        )
    )
) {
    val uiState by connectionDetailsViewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                "MQTT Connection",
                style = MaterialTheme.typography.headlineSmall
            )
        })
    }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            OutlinedTextField(
                value = uiState.clientID,
                onValueChange = { connectionDetailsViewModel.onClientIdChange(it) },
                label = { Text("Client ID") },
                isError = uiState.clientIDError != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.clientIDError != null) {
                Text(
                    text = uiState.clientIDError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = uiState.hostname,
                onValueChange = { connectionDetailsViewModel.onHostnameChange(it) },
                label = { Text("Hostname") },
                isError = uiState.hostnameError != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.hostnameError != null) {
                Text(
                    text = uiState.hostnameError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = uiState.port,
                onValueChange = { connectionDetailsViewModel.onPortChange(it) },
                label = { Text("Port") },
                isError = uiState.portError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.portError != null) {
                Text(
                    text = uiState.portError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = uiState.username,
                onValueChange = { connectionDetailsViewModel.onUsernameChange(it) },
                label = { Text("Username (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.password,
                onValueChange = {
                    connectionDetailsViewModel.onPasswordChange(it)
                },
                label = { Text("Password (Optional)") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    val image = if (passwordVisible) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    }

                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = uiState.useWebsockets,
                    onCheckedChange = { connectionDetailsViewModel.onUseWebsocketsChange(it) })
                Text("Use WebSockets")
            }

            OutlinedTextField(
                value = uiState.webSocketPath,
                enabled = uiState.useWebsockets,
                onValueChange = { connectionDetailsViewModel.onWebSocketPathChange(it) },
                label = { Text("WebSocket Path (default: /mqtt)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isConnecting) {
                CircularProgressIndicator()
            }

            if (uiState.connectionError != null) {
                Text(
                    text = uiState.connectionError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (uiState.connectionSuccess) {
                Text(
                    "Connection Successful!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }


            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onCancel() },
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = { connectionDetailsViewModel.connect() },
                    enabled = !uiState.isConnecting
                ) {
                    Text("Connect")
                }
            }
        }
    }
}
