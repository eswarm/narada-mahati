package `in`.eswarm.narada.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme // Changed import
import androidx.compose.material3.Text // Changed import
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import `in`.eswarm.narada.ui.theme.NaradaMQTTBrokerTheme // Added for Preview

@Composable
fun RegularPreference(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    RegularPreference(
        title = title,
        subtitle = AnnotatedString(text = subtitle),
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    )
}

@Composable
fun RegularPreference(
    title: String,
    subtitle: AnnotatedString,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val titleColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    val subtitleColor = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onClick,
            )
            .padding(all = 16.dp),
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
}

@Preview(showBackground = true)
@Composable
private fun RegularPreferencePreview() {
    NaradaMQTTBrokerTheme { // Added M3 theme for Preview
        Column {
            RegularPreference(
                title = "Advanced settings",
                subtitle = AnnotatedString(text = "Lorem ipsum dolor sit amet"),
                onClick = { },
            )
            RegularPreference(
                title = "Disabled Preference",
                subtitle = AnnotatedString(text = "This one is not clickable"),
                onClick = { },
                enabled = false
            )
        }
    }
}
