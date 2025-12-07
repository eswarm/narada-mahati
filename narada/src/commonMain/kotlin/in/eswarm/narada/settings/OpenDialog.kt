package `in`.eswarm.narada.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card // Changed import
import androidx.compose.material3.CardDefaults // Added for M3 Card elevation
import androidx.compose.material3.MaterialTheme // Changed import
import androidx.compose.material3.OutlinedTextField // Changed import
import androidx.compose.material3.Text // Changed import
import androidx.compose.material3.TextButton // Changed import
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import `in`.eswarm.narada.ui.theme.NaradaMQTTBrokerTheme // Added for Preview
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
https://stackoverflow.com/a/70588212
 */
@Composable
fun CustomDialog(
    openDialogCustom: MutableState<Boolean>,
    title: String,
    label: String,
    defaultValue: String = "",
    isNumber: Boolean = false,
    onOkRequest: (value: String) -> Unit
) {
    Dialog(onDismissRequest = { openDialogCustom.value = false } // This dismisses the Dialog window
    ) {
        CustomDialogUI(
            // openDialogCustom is passed to CustomDialogUI but its primary role
            // (dismissing the dialog) is now handled by onDismissRequest callback.
            openDialogCustom = openDialogCustom,
            title = title,
            label = label,
            value = defaultValue,
            isNumber = isNumber,
            onOkRequest = onOkRequest,
            onDismissRequest = { openDialogCustom.value = false } // Call to change state for Dialog window
        )
    }
}

//Layout
@Composable
fun CustomDialogUI(
    modifier: Modifier = Modifier,
    title: String,
    label: String,
    value: String,
    isNumber: Boolean,
    onOkRequest: (value: String) -> Unit,
    onDismissRequest: () -> Unit, // For UI actions within the Card
    @Suppress("UNUSED_PARAMETER") openDialogCustom: MutableState<Boolean> // Kept for API compatibility from SettingsScreen
) {
    Card( // M3 Card
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.padding(10.dp, 5.dp, 10.dp, 10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // M3 elevation
    ) {
        Column {
            var textValue by rememberSaveable { mutableStateOf(value) }

            Column(modifier = Modifier.padding(16.dp)) {
                Text( // M3 Text
                    text = title,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge, // M2 subtitle1 -> M3 titleLarge
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                OutlinedTextField( // M3 OutlinedTextField
                    label = { Text(label) }, // M3 Text
                    value = textValue,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (isNumber) {
                            KeyboardType.Number
                        } else {
                            KeyboardType.Text
                        }
                    ),
                    onValueChange = { textValue = it },
                    singleLine = true,
                    modifier = Modifier
                        .padding(top = 10.dp, start = 25.dp, end = 25.dp)
                        .fillMaxWidth()
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                TextButton(onClick = { // M3 TextButton
                    onDismissRequest() // UI action, triggers state change in CustomDialog
                }) {
                    Text( // M3 Text
                        "Cancel",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
                    )
                }
                TextButton(onClick = { // M3 TextButton
                    onOkRequest(textValue)
                    onDismissRequest() // UI action, triggers state change in CustomDialog
                }) {
                    Text( // M3 Text
                        "OK",
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
                    )
                }
            }
        }
    }
}


@Preview(name = "Custom Dialog")
@Composable
fun MyDialogUIPreview() {
    NaradaMQTTBrokerTheme { // Added M3 Theme for preview
        CustomDialogUI(
            openDialogCustom = remember { mutableStateOf(true) }, // Keep dialog open for preview
            title = "MQTT Port",
            label = "Port",
            value = "1883",
            onOkRequest = {},
            isNumber = true,
            onDismissRequest = {}
        )
    }
}
