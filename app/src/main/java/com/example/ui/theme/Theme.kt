package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CosmicColorScheme = darkColorScheme(
    primary = NeonPurple,
    secondary = NeonBlue,
    tertiary = HotPink,
    background = CosmicBackground,
    surface = DarkSpaceSurface,
    onPrimary = TextPrimary,
    onSecondary = CosmicBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = TranslucentGlassCard
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Disable dynamic coloring to save and secure our custom purple + blue neon aesthetic signature
    MaterialTheme(
        colorScheme = CosmicColorScheme,
        typography = Typography,
        content = content
    )
}
