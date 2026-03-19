package pub.hackers.android.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun lightColorSchemeFrom(colors: AppColorScheme) = lightColorScheme(
    primary = colors.accent,
    onPrimary = colors.background,
    primaryContainer = colors.surface,
    onPrimaryContainer = colors.textPrimary,
    secondary = colors.surface,
    onSecondary = colors.textPrimary,
    secondaryContainer = colors.surface,
    onSecondaryContainer = colors.textSecondary,
    tertiary = colors.textSecondary,
    onTertiary = colors.background,
    background = colors.background,
    onBackground = colors.textPrimary,
    surface = colors.background,
    onSurface = colors.textPrimary,
    surfaceVariant = colors.surface,
    onSurfaceVariant = colors.textSecondary,
    outline = colors.divider,
    outlineVariant = colors.divider,
    error = colors.reaction,
    onError = colors.background,
)

private fun darkColorSchemeFrom(colors: AppColorScheme) = darkColorScheme(
    primary = colors.accent,
    onPrimary = colors.background,
    primaryContainer = colors.surface,
    onPrimaryContainer = colors.textPrimary,
    secondary = colors.surface,
    onSecondary = colors.textPrimary,
    secondaryContainer = colors.surface,
    onSecondaryContainer = colors.textSecondary,
    tertiary = colors.textSecondary,
    onTertiary = colors.background,
    background = colors.background,
    onBackground = colors.textPrimary,
    surface = colors.background,
    onSurface = colors.textPrimary,
    surfaceVariant = colors.surface,
    onSurfaceVariant = colors.textSecondary,
    outline = colors.divider,
    outlineVariant = colors.divider,
    error = colors.reaction,
    onError = colors.background,
)

@Composable
fun HackersPubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val colorScheme = if (darkTheme) darkColorSchemeFrom(appColors) else lightColorSchemeFrom(appColors)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = appColors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalAppColors provides appColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
