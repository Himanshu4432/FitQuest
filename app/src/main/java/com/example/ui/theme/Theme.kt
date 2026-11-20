package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val FitQuestDarkColorScheme = darkColorScheme(
    primary = ElectricLime,
    onPrimary = OnAccentDark,
    primaryContainer = ElectricLimeContainer,
    onPrimaryContainer = ElectricLime,
    secondary = ElectricCyan,
    onSecondary = OnAccentDark,
    secondaryContainer = ElectricCyanContainer,
    onSecondaryContainer = ElectricCyan,
    tertiary = SolarAmber,
    onTertiary = OnAccentDark,
    tertiaryContainer = SolarAmberContainer,
    onTertiaryContainer = SolarAmber,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = ObsidianBg,
    surfaceContainerLow = ObsidianSurface,
    surfaceContainer = ObsidianCard,
    surfaceContainerHigh = ObsidianCardElevated,
    surfaceContainerHighest = ObsidianBorder,
    outline = ObsidianBorder,
    outlineVariant = Color(0xFF1F2D3D),
    error = CoralEnergy,
    onError = TextPrimary,
    errorContainer = CoralEnergyContainer,
    onErrorContainer = CoralEnergy
)

val FitQuestShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun FitQuestTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FitQuestDarkColorScheme,
        typography = FitQuestTypography,
        shapes = FitQuestShapes,
        content = content
    )
}
