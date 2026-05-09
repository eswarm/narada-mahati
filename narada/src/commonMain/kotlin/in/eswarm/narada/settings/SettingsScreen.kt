package `in`.eswarm.narada.settings

import `in`.eswarm.narada.preferences.AppPreferences
import `in`.eswarm.narada.util.showToast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
import `in`.eswarm.narada.resources.Res
import `in`.eswarm.narada.resources.empty_warning
import `in`.eswarm.narada.resources.enable_auth
import `in`.eswarm.narada.resources.enable_ws_title
import `in`.eswarm.narada.resources.mqtt_port_title
import `in`.eswarm.narada.resources.password
import `in`.eswarm.narada.system.BatteryOptimizationManager
import `in`.eswarm.narada.resources.path
import `in`.eswarm.narada.resources.port
import `in`.eswarm.narada.resources.port_warning
import `in`.eswarm.narada.resources.settings
import `in`.eswarm.narada.resources.settings_info
import `in`.eswarm.narada.resources.username
import `in`.eswarm.narada.resources.ws_path_title
import `in`.eswarm.narada.resources.ws_port_title
import `in`.eswarm.narada.resources.battery_optimization_summary
import `in`.eswarm.narada.resources.battery_optimization_title
import `in`.eswarm.narada.resources.wakelock_summary
import `in`.eswarm.narada.resources.wakelock_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel
) {
    val scrollState = rememberScrollState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {

            val showDialog = remember { mutableStateOf(false) }

            val mqttPort =
                settingsViewModel.appPreferences.mqttPort.collectAsState(initial = AppPreferences.MQTT_PORT_DEFAULT)
            val wsEnabled =
                settingsViewModel.appPreferences.wsEnabled.collectAsState(initial = AppPreferences.WS_ENABLED_DEFAULT)
            val wsPort =
                settingsViewModel.appPreferences.wsPort.collectAsState(initial = AppPreferences.WS_PORT_DEFAULT)
            val wsPath =
                settingsViewModel.appPreferences.wsPath.collectAsState(initial = AppPreferences.WS_PATH_DEFAULT)
            val authEnabled =
                settingsViewModel.appPreferences.authEnabled.collectAsState(initial = AppPreferences.AUTH_ENABLED_DEFAULT)
            val wakeLock =
                settingsViewModel.appPreferences.wakeLock.collectAsState(initial = AppPreferences.WAKELOCK_DEFAULT)
            val userName = settingsViewModel.appPreferences.userName.collectAsState(initial = "")
            val password = settingsViewModel.appPreferences.password.collectAsState(initial = "")

            val dialogTitle = rememberSaveable {
                mutableStateOf("")
            }

            val labelTitle = rememberSaveable {
                mutableStateOf("")
            }

            val defValue = rememberSaveable {
                mutableStateOf("")
            }

            val isNumber = rememberSaveable {
                mutableStateOf(false)
            }

            val dialogAction = remember {
                mutableStateOf(
                    { _: String -> }
                )
            }

            // Strings
            val mqttPortString = stringResource(Res.string.mqtt_port_title)
            val portString = stringResource(Res.string.port)
            val portWarningString = stringResource(Res.string.port_warning)
            val emptyWarningString = stringResource(Res.string.empty_warning)
            val userNameString = stringResource(Res.string.username)
            val passwordString = stringResource(Res.string.password)
            val wsPathString = stringResource(Res.string.ws_path_title)
            val wsPortString = stringResource(Res.string.ws_port_title)
            val pathString = stringResource(Res.string.path)

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

            if (showDialog.value) {
                CustomDialog(
                    openDialogCustom = showDialog,
                    dialogTitle.value,
                    labelTitle.value,
                    defValue.value,
                    isNumber.value,
                    onOkRequest = dialogAction.value
                )
            }

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

            RegularPreference(
                title = mqttPortString, subtitle = mqttPort.value.toString(), onClick = {
                    showDialog.value = true
                    dialogTitle.value = mqttPortString
                    labelTitle.value = portString
                    defValue.value = mqttPort.value.toString()
                    isNumber.value = true
                    dialogAction.value = { value: String ->
                        val isSuccess = settingsViewModel.setMqttPort(value)
                        if (!isSuccess) {
                            showToast(portWarningString)
                        }
                    }
                })

            HorizontalDivider()

            SwitchPreference(
                title = stringResource(Res.string.enable_ws_title),
                subtitle = wsEnabled.value.toString(),
                checked = wsEnabled.value,
                onCheckedChange = {
                    settingsViewModel.setWSEnabled(it)
                })

            HorizontalDivider()

            RegularPreference(title = wsPortString, subtitle = wsPort.value.toString(), onClick = {
                showDialog.value = true
                dialogTitle.value = wsPortString
                labelTitle.value = portString
                defValue.value = wsPort.value.toString()
                isNumber.value = true
                dialogAction.value = { value ->
                    val isSuccess = settingsViewModel.setWSPort(value)
                    if (!isSuccess) {
                        showToast(portWarningString)
                    }
                }
            })

            HorizontalDivider()

            RegularPreference(title = wsPathString, subtitle = wsPath.value, onClick = {
                showDialog.value = true
                dialogTitle.value = wsPathString
                labelTitle.value = pathString
                defValue.value = wsPath.value
                isNumber.value = false
                dialogAction.value = { value ->
                    settingsViewModel.setWSPath(value)
                }
            })

            HorizontalDivider()

            SwitchPreference(
                title = stringResource(Res.string.enable_auth),
                subtitle = authEnabled.value.toString(),
                checked = authEnabled.value,
                onCheckedChange = { value: Boolean ->
                    settingsViewModel.setAuthEnabled(value)
                })

            HorizontalDivider()

            RegularPreference(title = userNameString, subtitle = userName.value, onClick = {
                showDialog.value = true
                dialogTitle.value = userNameString
                labelTitle.value = userNameString
                isNumber.value = false
                defValue.value = userName.value
                dialogAction.value = { value ->
                    val isSuccess = settingsViewModel.setUserName(value)
                    if (!isSuccess) {
                        showToast(emptyWarningString)
                    }
                }
            })

            HorizontalDivider()

            RegularPreference(title = passwordString, subtitle = password.value, onClick = {
                showDialog.value = true
                dialogTitle.value = passwordString
                labelTitle.value = passwordString
                isNumber.value = false
                defValue.value = password.value
                dialogAction.value = { value ->
                    val isSuccess = settingsViewModel.setPassword(value)
                    if (!isSuccess) {
                        showToast(emptyWarningString)
                    }
                }
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

        }
    }
}
