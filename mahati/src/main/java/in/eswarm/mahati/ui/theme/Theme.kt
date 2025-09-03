package `in`.eswarm.mahati.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColorPalette = darkColorScheme(
    primary = greenConsole,
    secondary = redSecondary,
    background = blueBackground,
    surface = blueBackground,
    onBackground = Color.White,
    onSurface = Color.White,
    onSecondary = Color.White,
    onPrimary = Color.White
)

@Composable
fun NaradaMQTTBrokerTheme(
    content: @Composable () -> Unit
) {
    val colors = ColorPalette

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}