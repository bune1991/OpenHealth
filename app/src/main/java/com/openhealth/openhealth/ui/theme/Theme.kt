package com.openhealth.openhealth.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class AppColorScheme(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val cardBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val fabColor: Color,
    val isDark: Boolean
)

val DarkAppColors = AppColorScheme(
    background = SurfaceLowest,
    surface = SurfaceMid,
    surfaceVariant = SurfaceHigh,
    cardBackground = SurfaceMid,
    textPrimary = TextOnSurface,
    textSecondary = TextOnSurfaceVariant,
    textTertiary = TextSubtle,
    fabColor = SurfaceHigh,
    isDark = true
)

val LightAppColors = AppColorScheme(
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    cardBackground = LightCardBackground,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary,
    fabColor = LightFabColor,
    isDark = false
)

val LocalAppColors = compositionLocalOf { DarkAppColors }

private val DarkColorScheme = darkColorScheme(
    primary = ElectricIndigo,
    onPrimary = OnIndigo,
    primaryContainer = IndigoContainer,
    onPrimaryContainer = TextOnSurface,
    secondary = VibrantMagenta,
    onSecondary = OnMagenta,
    secondaryContainer = MagentaContainer,
    onSecondaryContainer = TextOnSurface,
    tertiary = SoftLavender,
    background = SurfaceLowest,
    onBackground = TextOnSurface,
    surface = SurfaceMid,
    onSurface = TextOnSurface,
    surfaceVariant = SurfaceHigh,
    onSurfaceVariant = TextOnSurfaceVariant,
    error = ErrorRed,
    onError = TextOnSurface,
    outline = TextSubtle,
    outlineVariant = GhostBorder,
    surfaceContainer = SurfaceMid,
    surfaceContainerHigh = SurfaceHigh,
    surfaceContainerHighest = SurfaceHighest,
    surfaceContainerLow = SurfaceLow,
    surfaceContainerLowest = SurfaceLowest,
    surfaceBright = SurfaceBright
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricIndigo,
    onPrimary = Color.White,
    primaryContainer = LightSurface,
    onPrimaryContainer = LightTextPrimary,
    secondary = VibrantMagenta,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    error = ErrorRed,
    onError = Color.White,
    outline = LightTextTertiary,
    outlineVariant = LightSurfaceVariant
)

@Composable
fun OpenHealthTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
