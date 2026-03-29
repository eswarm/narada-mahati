package `in`.eswarm.mahati.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.eswarm.mahati.resources.Res
import `in`.eswarm.mahati.resources.auto_reconnect_summary
import `in`.eswarm.mahati.resources.auto_reconnect_title
import `in`.eswarm.mahati.resources.settings
import `in`.eswarm.mahati.resources.settings_info
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel
) {
    val scrollState = rememberScrollState()
    val autoReconnect = settingsViewModel.settingsDataStore.autoReconnect.collectAsState(initial = false)

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = stringResource(Res.string.settings),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
            )

            Text(
                text = stringResource(Res.string.settings_info),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            HorizontalDivider()

            SwitchPreference(
                title = stringResource(Res.string.auto_reconnect_title),
                subtitle = stringResource(Res.string.auto_reconnect_summary),
                checked = autoReconnect.value,
                onCheckedChange = {
                    settingsViewModel.setAutoReconnect(it)
                })

            HorizontalDivider()
        }
    }
}

