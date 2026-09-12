package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = VoltagePrimary,
    onPrimary = VoltageOnPrimary,
    primaryContainer = VoltagePrimaryContainer,
    onPrimaryContainer = VoltageOnPrimaryContainer,
    secondary = VoltageSecondary,
    onSecondary = VoltageOnSecondary,
    secondaryContainer = VoltageSecondaryContainer,
    onSecondaryContainer = VoltageOnSecondary,
    tertiary = VoltageTertiary,
    onTertiary = VoltageOnTertiary,
    tertiaryContainer = VoltageTertiaryContainer,
    onTertiaryContainer = VoltageOnTertiary,
    background = VoltageSurface,
    onBackground = VoltageOnSurface,
    surface = VoltageSurfaceContainer,
    onSurface = VoltageOnSurface,
    surfaceVariant = VoltageSurfaceContainerHigh,
    onSurfaceVariant = VoltageOnSurfaceVariant,
    outline = VoltageOutline,
    outlineVariant = VoltageOutlineVariant
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
