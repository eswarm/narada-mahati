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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import `in`.eswarm.mahati.resources.*
import `in`.eswarm.mahati.system.BatteryOptimizationManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel
) {
    val scrollState = rememberScrollState()
    val autoReconnect = settingsViewModel.settingsDataStore.autoReconnect.collectAsState(initial = false)
    val wakeLock = settingsViewModel.settingsDataStore.wakeLock.collectAsState(initial = false)

    val lifecycleOwner = LocalLifecycleOwner.current
    val ignoreBatteryOptimization = remember { mutableStateOf(BatteryOptimizationManager.isIgnoringBatteryOptimizations()) }

    if (BatteryOptimizationManager.isSupported) {
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    ignoreBatteryOptimization.value = BatteryOptimizationManager.isIgnoringBatteryOptimizations()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

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

            if (BatteryOptimizationManager.isSupported) {
                HorizontalDivider()

                SwitchPreference(
                    title = stringResource(Res.string.battery_optimization_title),
                    subtitle = stringResource(Res.string.battery_optimization_summary),
                    checked = ignoreBatteryOptimization.value,
                    onCheckedChange = {
                        if (it && !ignoreBatteryOptimization.value) {
                            BatteryOptimizationManager.requestIgnoreBatteryOptimizations()
                        }
                    })
            }

            HorizontalDivider()

            SwitchPreference(
                title = stringResource(Res.string.wakelock_title),
                subtitle = stringResource(Res.string.wakelock_summary),
                checked = wakeLock.value,
                onCheckedChange = {
                    settingsViewModel.setWakeLock(it)
                })

            HorizontalDivider()
        }
    }
}
