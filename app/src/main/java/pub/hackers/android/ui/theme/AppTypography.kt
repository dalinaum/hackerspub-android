package pub.hackers.android.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class AppTextStyles(
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val bodyLargeSemiBold: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle,
    val caption: TextStyle,
    val tabLabel: TextStyle,
)

val AppTypographyDefaults = AppTextStyles(
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
    titleMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
    bodyLargeSemiBold = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    labelMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal),
    labelSmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    caption = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
    tabLabel = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
)

val LocalAppTypography = staticCompositionLocalOf { AppTypographyDefaults }
