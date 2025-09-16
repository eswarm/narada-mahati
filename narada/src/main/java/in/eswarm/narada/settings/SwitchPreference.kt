package `in`.eswarm.narada.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import `in`.eswarm.narada.ui.theme.NaradaMQTTBrokerTheme

@Composable
fun SwitchPreference(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    SwitchPreference(
        title = title,
        subtitle = AnnotatedString(text = subtitle),
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
    )
}

@Composable
fun SwitchPreference(
    title: String,
    subtitle: AnnotatedString,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val titleColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    val subtitleColor = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                enabled = enabled,
                onClick = { onCheckedChange(!checked) },
            )
            .padding(all = 16.dp),
    ) {
        Column(
            modifier = Modifier.weight(weight = 1f, fill = true),
        ) {
            Text( // M3 Text
                text = title,
                style = MaterialTheme.typography.bodyLarge, // M2 body1 -> M3 bodyLarge
                color = titleColor
            )

            Text( // M3 Text
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium, // M2 body2 -> M3 bodyMedium
                color = subtitleColor
            )
        }

        Switch( // M3 Switch
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled, // Pass enabled state to Switch as well
            interactionSource = interactionSource,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SwitchPreferencePreview() {
    var checked by remember { mutableStateOf(value = true) }
    var enabled by remember { mutableStateOf(value = true) }

    NaradaMQTTBrokerTheme { // Added M3 theme for Preview
        Column {
            SwitchPreference(
                title = "Dark theme",
                subtitle = AnnotatedString(text = "Enable or disable dark theme"),
                checked = checked,
                onCheckedChange = { checked = it },
                enabled = enabled
            )
            SwitchPreference(
                title = "Advanced settings",
                subtitle = AnnotatedString(text = "This switch is disabled"),
                checked = false,
                onCheckedChange = { /* No-op */ },
                enabled = false
            )
             SwitchPreference(
                title = "Toggle Enabled State of First Switch",
                subtitle = AnnotatedString(text = "Click to toggle enabled state of 'Dark theme' switch"),
                checked = enabled,
                onCheckedChange = { enabled = it }
            )
        }
    }
}
