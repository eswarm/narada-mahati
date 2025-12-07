package `in`.eswarm.narada.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes // Already M3
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp), // M3 default, consistent with your current small/medium
    small = RoundedCornerShape(4.dp),      // Your existing small value
    medium = RoundedCornerShape(4.dp),     // Your existing medium value
    large = RoundedCornerShape(0.dp),      // Your existing large value
    extraLarge = RoundedCornerShape(0.dp)  // Consistent with your large = 0.dp
)
