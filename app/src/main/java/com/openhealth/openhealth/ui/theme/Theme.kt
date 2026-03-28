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

// ═══════════════════════════════════════════════════════════
// Nocturne (dark, default) — Electric Indigo on OLED black
// ═══════════════════════════════════════════════════════════

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

// ═══════════════════════════════════════════════════════════
// Solar (warm light) — Deep orange / gold on warm white
// ═══════════════════════════════════════════════════════════

private val SolarBackground = Color(0xFFFFF8F0)
private val SolarSurface = Color(0xFFFFFFFF)
private val SolarSurfaceVariant = Color(0xFFFFEDD5)
private val SolarPrimary = Color(0xFFFF8C00)
private val SolarSecondary = Color(0xFFFFD700)
private val SolarOnBackground = Color(0xFF1A1A1A)
private val SolarOnSurface = Color(0xFF1A1A1A)
private val SolarTextSecondary = Color(0xFF6B5B3A)
private val SolarTextTertiary = Color(0xFF9A8A6A)

val SolarAppColors = AppColorScheme(
    background = SolarBackground,
    surface = SolarSurface,
    surfaceVariant = SolarSurfaceVariant,
    cardBackground = SolarSurface,
    textPrimary = SolarOnBackground,
    textSecondary = SolarTextSecondary,
    textTertiary = SolarTextTertiary,
    fabColor = SolarSurfaceVariant,
    isDark = false
)

private val SolarColorScheme = lightColorScheme(
    primary = SolarPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0B2),
    onPrimaryContainer = SolarOnBackground,
    secondary = SolarSecondary,
    onSecondary = Color(0xFF3A3000),
    secondaryContainer = Color(0xFFFFF3C4),
    onSecondaryContainer = SolarOnBackground,
    tertiary = Color(0xFFFF6B35),
    background = SolarBackground,
    onBackground = SolarOnBackground,
    surface = SolarSurface,
    onSurface = SolarOnSurface,
    surfaceVariant = SolarSurfaceVariant,
    onSurfaceVariant = SolarTextSecondary,
    error = ErrorRed,
    onError = Color.White,
    outline = SolarTextTertiary,
    outlineVariant = Color(0xFFE8D5B5)
)

// ═══════════════════════════════════════════════════════════
// Ocean (cool dark) — Ocean blue / cyan on deep ocean
// ═══════════════════════════════════════════════════════════

private val OceanBackground = Color(0xFF001219)
private val OceanSurface = Color(0xFF002233)
private val OceanSurfaceVariant = Color(0xFF003344)
private val OceanSurfaceHighest = Color(0xFF004455)
private val OceanPrimary = Color(0xFF0077B6)
private val OceanSecondary = Color(0xFF00B4D8)
private val OceanTertiary = Color(0xFF90E0EF)
private val OceanOnBackground = Color(0xFFE0F7FF)
private val OceanOnSurface = Color(0xFFE0F7FF)
private val OceanTextSecondary = Color(0xFF8CB4C4)
private val OceanTextTertiary = Color(0xFF5A8899)

val OceanAppColors = AppColorScheme(
    background = OceanBackground,
    surface = OceanSurface,
    surfaceVariant = OceanSurfaceVariant,
    cardBackground = OceanSurface,
    textPrimary = OceanOnBackground,
    textSecondary = OceanTextSecondary,
    textTertiary = OceanTextTertiary,
    fabColor = OceanSurfaceVariant,
    isDark = true
)

private val OceanColorScheme = darkColorScheme(
    primary = OceanPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF005580),
    onPrimaryContainer = OceanOnBackground,
    secondary = OceanSecondary,
    onSecondary = Color(0xFF003040),
    secondaryContainer = Color(0xFF006080),
    onSecondaryContainer = OceanOnBackground,
    tertiary = OceanTertiary,
    background = OceanBackground,
    onBackground = OceanOnBackground,
    surface = OceanSurface,
    onSurface = OceanOnSurface,
    surfaceVariant = OceanSurfaceVariant,
    onSurfaceVariant = OceanTextSecondary,
    error = ErrorRed,
    onError = OceanOnBackground,
    outline = OceanTextTertiary,
    outlineVariant = Color(0xFF1A3A4A),
    surfaceContainer = OceanSurface,
    surfaceContainerHigh = OceanSurfaceVariant,
    surfaceContainerHighest = OceanSurfaceHighest,
    surfaceContainerLow = Color(0xFF001A26),
    surfaceContainerLowest = OceanBackground,
    surfaceBright = Color(0xFF005566)
)

// ═══════════════════════════════════════════════════════════
// Forest (nature dark) — Emerald / mint on deep green
// ═══════════════════════════════════════════════════════════

private val ForestBackground = Color(0xFF0A1F0A)
private val ForestSurface = Color(0xFF122212)
private val ForestSurfaceVariant = Color(0xFF1A3318)
private val ForestSurfaceHighest = Color(0xFF224422)
private val ForestPrimary = Color(0xFF10B981)
private val ForestSecondary = Color(0xFF34D399)
private val ForestTertiary = Color(0xFF6EE7B7)
private val ForestOnBackground = Color(0xFFE0FFE0)
private val ForestOnSurface = Color(0xFFE0FFE0)
private val ForestTextSecondary = Color(0xFF8CB48C)
private val ForestTextTertiary = Color(0xFF5A8A5A)

val ForestAppColors = AppColorScheme(
    background = ForestBackground,
    surface = ForestSurface,
    surfaceVariant = ForestSurfaceVariant,
    cardBackground = ForestSurface,
    textPrimary = ForestOnBackground,
    textSecondary = ForestTextSecondary,
    textTertiary = ForestTextTertiary,
    fabColor = ForestSurfaceVariant,
    isDark = true
)

private val ForestColorScheme = darkColorScheme(
    primary = ForestPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0D8060),
    onPrimaryContainer = ForestOnBackground,
    secondary = ForestSecondary,
    onSecondary = Color(0xFF003020),
    secondaryContainer = Color(0xFF1A6040),
    onSecondaryContainer = ForestOnBackground,
    tertiary = ForestTertiary,
    background = ForestBackground,
    onBackground = ForestOnBackground,
    surface = ForestSurface,
    onSurface = ForestOnSurface,
    surfaceVariant = ForestSurfaceVariant,
    onSurfaceVariant = ForestTextSecondary,
    error = ErrorRed,
    onError = ForestOnBackground,
    outline = ForestTextTertiary,
    outlineVariant = Color(0xFF1A3A1A),
    surfaceContainer = ForestSurface,
    surfaceContainerHigh = ForestSurfaceVariant,
    surfaceContainerHighest = ForestSurfaceHighest,
    surfaceContainerLow = Color(0xFF0D1A0D),
    surfaceContainerLowest = ForestBackground,
    surfaceBright = Color(0xFF2A5528)
)

// ═══════════════════════════════════════════════════════════
// Light (clean light) — Indigo on neutral white
// ═══════════════════════════════════════════════════════════

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

val LocalAppColors = compositionLocalOf { DarkAppColors }

@Composable
fun OpenHealthTheme(
    themeName: String = "nocturne",
    content: @Composable () -> Unit
) {
    val (colorScheme, appColors, isDark) = when (themeName) {
        "solar" -> Triple(SolarColorScheme, SolarAppColors, false)
        "ocean" -> Triple(OceanColorScheme, OceanAppColors, true)
        "forest" -> Triple(ForestColorScheme, ForestAppColors, true)
        "light" -> Triple(LightColorScheme, LightAppColors, false)
        else -> Triple(DarkColorScheme, DarkAppColors, true) // nocturne
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
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
