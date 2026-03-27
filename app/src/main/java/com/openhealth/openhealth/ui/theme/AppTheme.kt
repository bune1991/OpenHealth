package com.openhealth.openhealth.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Theme-aware color accessors.
 * Use these instead of the raw Color.kt constants to support light/dark theme.
 */
object AppTheme {
    val background: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current.background

    val surface: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current.surface

    val surfaceVariant: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current.surfaceVariant

    val cardBackground: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current.cardBackground

    val textPrimary: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current.textPrimary

    val textSecondary: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current.textSecondary

    val textTertiary: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current.textTertiary

    val fabColor: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current.fabColor

    val isDark: Boolean
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current.isDark
}
