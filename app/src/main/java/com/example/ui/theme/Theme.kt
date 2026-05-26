package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    theme: LoopTheme = LoopTheme.MIDNIGHT_PULSE,
    songTitle: String = "",
    content: @Composable () -> Unit
) {
    // Determine the calculated color set matching our state specifications
    val colors = when (theme) {
        LoopTheme.MIDNIGHT_PULSE -> MidnightPulseColors
        LoopTheme.SUNSET_VIBES -> SunsetVibesColors
        LoopTheme.AURORA -> AuroraColors
        LoopTheme.VELVET_DARK -> VelvetDarkColors
        LoopTheme.PURE_LIGHT -> PureLightColors
        LoopTheme.DYNAMIC_REACTIVE -> getSongReactiveColors(songTitle)
    }

    // Set up standard Material 3 Scheme mapping to keep standard M3 widgets looking beautiful and aligned automatically
    val materialScheme = if (colors.isDark) {
        darkColorScheme(
            primary = colors.primary,
            secondary = colors.secondary,
            tertiary = colors.tertiary,
            background = colors.background,
            surface = colors.surface,
            onPrimary = if (theme == LoopTheme.SUNSET_VIBES) colors.background else Color(0xFFFAF9F6),
            onSecondary = colors.background,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.translucentCard
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            secondary = colors.secondary,
            tertiary = colors.tertiary,
            background = colors.background,
            surface = colors.surface,
            onPrimary = androidx.compose.ui.graphics.Color.White,
            onSecondary = colors.textPrimary,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.translucentCard
        )
    }

    CompositionLocalProvider(
        LocalLoopColors provides colors
    ) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = Typography,
            content = content
        )
    }
}
