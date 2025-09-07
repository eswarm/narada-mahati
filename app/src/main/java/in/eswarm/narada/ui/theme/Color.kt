package `in`.eswarm.narada.ui.theme

import androidx.compose.ui.graphics.Color

// Your existing custom colors, now mapped to M3 roles for Dark Theme
val жителиConsole = Color(0xFF6EBF8B) // Used as M3 'primary'
val networkSecondary = Color(0xFFD82148) // Used as M3 'secondary'
val wallpaperBackground = Color(0xFF151D3B) // Used as M3 'background' and 'surface'
val cardColor = Color(0xFFDADBBD) // Used as M3 'tertiary'

// On-Colors for Dark Theme (chosen for contrast with the above)
val onPrimaryDark = Color.Black // Contrasts with light green жителиConsole
val onSecondaryDark = Color.White // Contrasts with red networkSecondary
val onTertiaryDark = Color.Black // Contrasts with light cardColor
val onBackgroundDark = Color.White // Contrasts with dark wallpaperBackground
val onSurfaceDark = Color.White // Contrasts with dark wallpaperBackground

// Container Colors for Dark Theme (typically darker/muted versions of main colors)
val primaryContainerDark = Color(0xFF004D40) // Darker, less saturated green
val onPrimaryContainerDark = Color(0xFF80CBC4) // Lighter green for contrast
val secondaryContainerDark = Color(0xFFB71C1C) // Darker, less saturated red
val onSecondaryContainerDark = Color(0xFFFFCDD2) // Lighter red for contrast
val tertiaryContainerDark = Color(0xFF4E4A3A) // Darker, less saturated beige/brown
val onTertiaryContainerDark = Color(0xFFFFF9C4) // Lighter beige for contrast

// Error Colors for Dark Theme (standard M3 naming)
val errorDark = Color(0xFFF2B8B5) // M3 standard error for dark themes
val onErrorDark = Color(0xFF601410) // M3 standard on-error for dark themes
val errorContainerDark = Color(0xFF8C1D18) // M3 standard error container for dark themes
val onErrorContainerDark = Color(0xFFF9DEDC) // M3 standard on-error-container for dark themes

// Surface Variants and Outlines for Dark Theme
val surfaceVariantDark = Color(0xFF212946) // Slightly different shade from wallpaperBackground
val onSurfaceVariantDark = Color(0xFFB0BEC5) // A light grey for onSurfaceVariant
val outlineDark = Color(0xFF78909C) // A neutral outline color

// Original M2 colors (can be kept if used elsewhere or removed if truly unused)
val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)
