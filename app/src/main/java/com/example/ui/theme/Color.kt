package com.example.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Core brand primary colors for reference
val NeonPurple = Color(0xFFA855F7) // Purple-500
val NeonBlue = Color(0xFF3B82F6) // Blue-500
val HotPink = Color(0xFFEC4899) // Pink-500
val ActiveGreen = Color(0xFF10B981) // Emerald-500

// Font Color reference grades
val TextPrimary = Color(0xFFF8FAFC) // Slate-50 stable white text
val TextSubdued = Color(0xFF94A3B8) // Slate-400 subtle grey text
val DarkBorderNeon = Color(0x1FFFFFFF)
val DarkSpaceSurface = Color(0x12FFFFFF)
val CosmicBackground = Color(0xFF030310)
val TranslucentGlassCard = Color(0x13FFFFFF)

enum class LoopTheme(val displayName: String, val isDark: Boolean) {
    MIDNIGHT_PULSE("Midnight Pulse", true),
    SUNSET_VIBES("Sunset Vibes", true),
    AURORA("Aurora", true),
    VELVET_DARK("Velvet Dark", true),
    PURE_LIGHT("Pure Light", false),
    DYNAMIC_REACTIVE("Dynamic Aura 🎵", true)
}

data class LoopColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val surface: Color,
    val translucentCard: Color,
    val textPrimary: Color,
    val textSubdued: Color,
    val border: Color,
    val glowColors: List<Color>,
    val isDark: Boolean
)

val MidnightPulseColors = LoopColors(
    primary = Color(0xFFA855F7), // Neon Purple
    secondary = Color(0xFF3B82F6), // Neon Blue
    tertiary = Color(0xFFEC4899), // Hot Pink
    background = Color(0xFF030310), // Deep blue-velvet night
    surface = Color(0x12FFFFFF), // 7% white frosting
    translucentCard = Color(0x1CFFFFFF), // 11% white glass
    textPrimary = Color(0xFFF8FAFC),
    textSubdued = Color(0xFF94A3B8),
    border = Color(0x1BFFFFFF),
    glowColors = listOf(Color(0x33A855F7), Color(0x2B3B82F6)),
    isDark = true
)

val SunsetVibesColors = LoopColors(
    primary = Color(0xFFF97316), // Warm Tangerine
    secondary = Color(0xFFD946EF), // Soft Magenta
    tertiary = Color(0xFF8B5CF6), // Royal Purple
    background = Color(0xFF0D060D), // Soft sunset charcoal
    surface = Color(0x19FFFFFF), // Warm 10% overlay
    translucentCard = Color(0x21FFFFFF), // 13% warm glass
    textPrimary = Color(0xFFFEF3C7), // Warm eggshell text
    textSubdued = Color(0xFFD1D5DB), // Cozy warm grey
    border = Color(0x1CFFE4E6), // Warm roseate border accent
    glowColors = listOf(Color(0x38F97316), Color(0x2AD946EF)),
    isDark = true
)

val AuroraColors = LoopColors(
    primary = Color(0xFF22D3EE), // Ice Cyan
    secondary = Color(0xFF34D399), // Aurora Emerald
    tertiary = Color(0xFF10B981), // Fresh Mint
    background = Color(0xFF020A0B), // Calm glacial depth
    surface = Color(0x14FFFFFF), // Glacial frosted 8%
    translucentCard = Color(0x1EFFFFFF), // Ice sheet 12%
    textPrimary = Color(0xFFECFDF5), // Glacier white
    textSubdued = Color(0xFFA7F3D0), // Aurora green sheen
    border = Color(0x1F22D3EE),
    glowColors = listOf(Color(0x2E22D3EE), Color(0x2334D399)),
    isDark = true
)

val VelvetDarkColors = LoopColors(
    primary = Color(0xFFE11D48), // Deep Cinematic Crimson
    secondary = Color(0xFFF43F5E), // Rose Petal accent
    tertiary = Color(0xFF18181B), // Charcoal velvet
    background = Color(0xFF070204), // Pure stage dark cinema back
    surface = Color(0x12FFFFFF),
    translucentCard = Color(0x1AFFFFFF),
    textPrimary = Color(0xFFFFF1F2),
    textSubdued = Color(0xFFFDA4AF),
    border = Color(0x1FF43F5E),
    glowColors = listOf(Color(0x30E11D48), Color(0x1AF43F5E)),
    isDark = true
)

val PureLightColors = LoopColors(
    primary = Color(0xFF4F46E5), // Royal Indigo
    secondary = Color(0xFF0891B2), // Soft Teal
    tertiary = Color(0xFFE11D48), // Scarlet touch
    background = Color(0xFFF5F5F7), // Elegant Apple-like bright background
    surface = Color(0xFFFFFFFF), // Pure white cards list
    translucentCard = Color(0xFFE2E8F0), // Fine light gray card base
    textPrimary = Color(0xFF1E293B), // Charcoal dark text
    textSubdued = Color(0xFF64748B), // Clear steel-grey subtitles
    border = Color(0xFFE2E8F0), // Clean layout dividers
    glowColors = listOf(Color(0x1F4F46E5), Color(0x140891B2)),
    isDark = false
)

fun getSongReactiveColors(songTitle: String): LoopColors {
    val title = songTitle.lowercase()
    return when {
         title.contains("acid") || title.contains("edm") || title.contains("techno") || title.contains("remix") || title.contains("synth") || title.contains("groove") -> {
             // High energy: electric hot pink + cyan neon vibe
             LoopColors(
                 primary = Color(0xFFEC4899), // Hot Pink
                 secondary = Color(0xFF22D3EE), // Electric Cyan
                 tertiary = Color(0xFF8B5CF6),
                 background = Color(0xFF06020E),
                 surface = Color(0x19FFFFFF),
                 translucentCard = Color(0x24FFFFFF),
                 textPrimary = Color(0xFFFCFDFE),
                 textSubdued = Color(0xFFA5B4FC),
                 border = Color(0x22EC4899),
                 glowColors = listOf(Color(0x30EC4899), Color(0x3022D3EE)),
                 isDark = true
             )
         }
         title.contains("chill") || title.contains("lofi") || title.contains("dream") || title.contains("ambient") || title.contains("study") -> {
             // Warm tea, lilac & cozy amber study atmosphere
             LoopColors(
                 primary = Color(0xFFC084FC), // Lavender
                 secondary = Color(0xFFFBCFE8), // Pink glow
                 tertiary = Color(0xFFFDE68A), // Warm gold tea
                 background = Color(0xFF0F0C1B),
                 surface = Color(0x12FFFFFF),
                 translucentCard = Color(0x1BFFFFFF),
                 textPrimary = Color(0xFFFFF9FA),
                 textSubdued = Color(0xFFD8B4FE),
                 border = Color(0x1BC084FC),
                 glowColors = listOf(Color(0x2BC084FC), Color(0x20FDE68A)),
                 isDark = true
             )
         }
         title.contains("acoustic") || title.contains("guitar") || title.contains("live") || title.contains("vocal") || title.contains("unplugged") -> {
             // Fresh soft woodwinds and clean green accents
             LoopColors(
                 primary = Color(0xFF34D399), // Fresh Mint
                 secondary = Color(0xFFF59E0B), // Golden Sunset
                 tertiary = Color(0xFFECFDF5),
                 background = Color(0xFF040C08),
                 surface = Color(0x14FFFFFF),
                 translucentCard = Color(0x1DFFFFFF),
                 textPrimary = Color(0xFFF0FDF4),
                 textSubdued = Color(0xFFA7F3D0),
                 border = Color(0x1A34D399),
                 glowColors = listOf(Color(0x2C34D399), Color(0x1BF59E0B)),
                 isDark = true
             )
         }
         else -> {
             // Default elegant midnight pulse
             MidnightPulseColors
         }
    }
}

val LocalLoopColors = staticCompositionLocalOf { MidnightPulseColors }
