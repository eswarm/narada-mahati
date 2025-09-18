package `in`.eswarm.mahati.connection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.eswarm.mahati.AppComponent

@Composable
fun NewConnectionScreen(
    appComponent: AppComponent, connectionViewModel: ConnectionViewModel = viewModel(
        factory = ConnectionViewModel.Factory(
            appComponent.mqttManager,
            appComponent.connectionRepo
        )
    )
) {
    val uiState by connectionViewModel.uiState.collectAsState() // Consider collectAsStateWithLifecycle for better lifecycle management

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("MQTT Connection", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = uiState.clientID,
            onValueChange = { connectionViewModel.onClientIdChange(it) },
            label = { Text("Client ID") },
            isError = uiState.clientIdError != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (uiState.clientIdError != null) {
            Text(
                text = uiState.clientIdError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        OutlinedTextField(
            value = uiState.hostname,
            onValueChange = { connectionViewModel.onHostnameChange(it) },
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
            onValueChange = { connectionViewModel.onPortChange(it) },
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
            onValueChange = { connectionViewModel.onUsernameChange(it) },
            label = { Text("Username (Optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { connectionViewModel.onPasswordChange(it) },
            label = { Text("Password (Optional)") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = uiState.useSsl,
                onCheckedChange = { connectionViewModel.onUseSslChange(it) })
            Text("Enable SSL/TLS")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = uiState.useWebsockets,
                onCheckedChange = { connectionViewModel.onUseWebsocketsChange(it) })
            Text("Use WebSockets")
        }

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
                onClick = { connectionViewModel.cancel() },
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text("Cancel")
            }
            Button(
                onClick = { connectionViewModel.connect() }, enabled = !uiState.isConnecting
            ) {
                Text("Connect")
            }
        }
    }
}
