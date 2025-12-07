package `in`.eswarm.mahati.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import `in`.eswarm.mahati.resources.Res
import `in`.eswarm.mahati.resources.firacode_bold
import `in`.eswarm.mahati.resources.firacode_light
import `in`.eswarm.mahati.resources.firacode_regular
import org.jetbrains.compose.resources.Font

@Composable
fun firaCodeFontFamily(): FontFamily {
    return FontFamily(
        Font(Res.font.firacode_light, FontWeight.Light),
        Font(Res.font.firacode_regular, FontWeight.Normal),
        Font(Res.font.firacode_bold, FontWeight.Bold)
    )
}

@Composable
fun appTypography(): Typography {
    val firaCode = firaCodeFontFamily()
    val defaultTypography = Typography() // Get default Material 3 typography

    return Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = firaCode),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = firaCode),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = firaCode),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = firaCode),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = firaCode),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = firaCode),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = firaCode),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = firaCode),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = firaCode),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = firaCode),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = firaCode),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = firaCode),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = firaCode),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = firaCode),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = firaCode)
    )
}

