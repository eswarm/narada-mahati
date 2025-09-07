package `in`.eswarm.narada.settings

import `in`.eswarm.narada.R
import `in`.eswarm.narada.preferences.AppPreferences
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider // Changed import
import androidx.compose.material3.MaterialTheme   // Changed import
import androidx.compose.material3.Scaffold        // Changed import
import androidx.compose.material3.Text            // Changed import
import androidx.compose.material3.ExperimentalMaterial3Api // Added for Scaffold if specific features are used
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class) // Added for M3 Scaffold
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel
) {
    val scrollState = rememberScrollState()

    Scaffold { innerPadding -> // M3 Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // This padding comes from the M3 Scaffold
                .verticalScroll(scrollState)
                .windowInsetsPadding(WindowInsets.statusBars) // Apply status bar padding inside the scrollable column
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

            val context = LocalContext.current

            // Strings
            val mqttPortString = stringResource(id = R.string.mqtt_port_title)
            val portString = stringResource(id = R.string.port)
            val portWarningString = stringResource(id = R.string.port_warning)
            val emptyWarningString = stringResource(id = R.string.empty_warning)
            val userNameString = stringResource(id = R.string.username)
            val passwordString = stringResource(id = R.string.password)
            val wsPathString = stringResource(id = R.string.ws_path_title)
            val wsPortString = stringResource(id = R.string.ws_port_title)
            val pathString = stringResource(id = R.string.path)

            if (showDialog.value) {
                // Assuming CustomDialog will be updated to M3 or is M3 compatible.
                // If CustomDialog uses M2 components internally, it will need migration.
                CustomDialog(
                    openDialogCustom = showDialog,
                    dialogTitle.value,
                    labelTitle.value, // Corrected parameter name from dialogTitle.value to labelTitle.value based on typical use
                    defValue.value,
                    isNumber.value,
                    onOkRequest = dialogAction.value
                )
            }

            Text( // M3 Text
                text = stringResource(id = R.string.settings),
                style = MaterialTheme.typography.headlineLarge, // M2 h3 -> M3 headlineLarge
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
            )

            Text( // M3 Text
                text = stringResource(id = R.string.settings_info),
                style = MaterialTheme.typography.bodyLarge, // M2 body1 -> M3 bodyLarge
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            HorizontalDivider() // M3 HorizontalDivider

            // Assuming RegularPreference will be updated to M3 or is M3 compatible.
            // If it uses M2 components internally, it will need migration.
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
                            Toast.makeText(
                                context, portWarningString, Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })

            HorizontalDivider() // M3 HorizontalDivider

            // Assuming SwitchPreference will be updated to M3 or is M3 compatible.
            // If it uses M2 Switch or Text internally, it will need migration.
            SwitchPreference(
                title = stringResource(id = R.string.enable_ws_title),
                subtitle = wsEnabled.value.toString(),
                checked = wsEnabled.value,
                onCheckedChange = {
                    settingsViewModel.setWSEnabled(it)
                })

            HorizontalDivider() // M3 HorizontalDivider

            RegularPreference(title = wsPortString, subtitle = wsPort.value.toString(), onClick = {
                showDialog.value = true
                dialogTitle.value = wsPortString
                labelTitle.value = portString
                defValue.value = wsPort.value.toString()
                isNumber.value = true
                dialogAction.value = { value ->
                    val isSuccess = settingsViewModel.setWSPort(value)
                    if (!isSuccess) {
                        Toast.makeText(
                            context, portWarningString, Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })

            HorizontalDivider() // M3 HorizontalDivider

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

            HorizontalDivider() // M3 HorizontalDivider

            SwitchPreference(
                title = stringResource(id = R.string.enable_auth),
                subtitle = authEnabled.value.toString(),
                checked = authEnabled.value,
                onCheckedChange = { value: Boolean ->
                    settingsViewModel.setAuthEnabled(value)
                })

            HorizontalDivider() // M3 HorizontalDivider

            RegularPreference(title = userNameString, subtitle = userName.value, onClick = {
                showDialog.value = true
                dialogTitle.value = userNameString
                labelTitle.value = userNameString
                isNumber.value = false
                defValue.value = userName.value
                dialogAction.value = { value ->
                    val isSuccess = settingsViewModel.setUserName(value)
                    if (!isSuccess) {
                        Toast.makeText(
                            context, emptyWarningString, Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })

            HorizontalDivider() // M3 HorizontalDivider

            RegularPreference(title = passwordString, subtitle = password.value, onClick = {
                showDialog.value = true
                dialogTitle.value = passwordString
                labelTitle.value = passwordString
                isNumber.value = false
                defValue.value = password.value
                dialogAction.value = { value ->
                    val isSuccess = settingsViewModel.setPassword(value)
                    if (!isSuccess) {
                        Toast.makeText(
                            context, emptyWarningString, Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NaradaMQTTBrokerTheme {
        SettingsScreen() // This would need a SettingsViewModel instance
    }
}
*/
