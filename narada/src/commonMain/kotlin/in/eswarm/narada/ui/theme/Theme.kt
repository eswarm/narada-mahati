package `in`.eswarm.narada.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb

// Define the Material 3 Dark Color Scheme using the colors from Color.kt
private val DarkColorScheme = darkColorScheme(
    primary = console,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = networkSecondary,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = cardColor,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = wallpaperBackground,
    onBackground = onBackgroundDark,
    surface = wallpaperBackground, // Often same as background for dark themes
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark
)

@Composable
fun NaradaMQTTBrokerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // For now, we only have a dark theme defined
    content: @Composable () -> Unit
) {
    // We are only using the DarkColorScheme as per the original setup.
    // If a light theme is needed in the future, a lightColorScheme would be defined and chosen here.
    val colorScheme = DarkColorScheme

    /*
    // TODO :: make this work both for Droid and Desktop.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb() // Or background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

     */

    MaterialTheme(
        colorScheme = colorScheme,
        typography = appTypography(),
        shapes = Shapes,
        content = content
    )
}
