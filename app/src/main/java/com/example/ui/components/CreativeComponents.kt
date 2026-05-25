package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

import androidx.compose.ui.text.style.TextAlign

/**
 * Majestic resolution-independent ambient space background with top-left purple
 * and bottom-right blue glowing blurs, exactly replicating the Frosted Glass design theme.
 */
fun Modifier.frostedGlassBackground(): Modifier = this.then(
    Modifier.drawBehind {
        // Step 1: Deep velvet black solid base color
        drawRect(color = Color(0xFF020205))

        // Step 2: Animated/Subtle Top-Left glowing purple spot
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x26A855F7), // Purple-500 @ 15% opacity
                    Color.Transparent
                ),
                center = Offset(size.width * -0.05f, size.height * -0.05f),
                radius = size.width * 1.0f
            ),
            radius = size.width * 1.0f,
            center = Offset(size.width * -0.05f, size.height * -0.05f)
        )

        // Step 3: Subtle Bottom-Right glowing blue spot
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x263B82F6), // Blue-500 @ 15% opacity
                    Color.Transparent
                ),
                center = Offset(size.width * 1.05f, size.height * 1.05f),
                radius = size.width * 1.0f
            ),
            radius = size.width * 1.0f,
            center = Offset(size.width * 1.05f, size.height * 1.05f)
        )
    }
)

/**
 * Translucent Glassmorphism Card Container with elegant frosted glass borders.
 */
@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.White,
    hasGlow: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp), // 24dp matches rounded-3xl beautifully
    content: @Composable ColumnScope.() -> Unit
) {
    // Beautiful glassy border resembling border-white/10 to border-white/20
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.2f),
            Color.White.copy(alpha = 0.05f)
        )
    )

    // A subtle accent halo behind the card to create depth if highlighted/hasGlow
    val shadowModifier = if (hasGlow) {
        Modifier.shadow(
            elevation = 12.dp,
            shape = shape,
            ambientColor = borderColor.copy(alpha = 0.4f),
            spotColor = borderColor.copy(alpha = 0.4f)
        )
    } else Modifier

    Column(
        modifier = modifier
            .then(shadowModifier)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0x24FFFFFF), // Frosted glassy top-half glare (White @ 14%)
                        Color(0x0EFFFFFF)  // Lower-half deep matte (White @ 5%)
                    )
                )
            )
            .border(1.dp, borderBrush, shape)
            .padding(18.dp),
        content = content
    )
}

/**
 * Solid Premium panel container with beautiful frosted glow borders.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val glassShape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .clip(glassShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0x1AFFFFFF), // Frosted white at 10%
                        Color(0x0AFFFFFF)  // White at 4%
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                glassShape
            )
            .padding(16.dp),
        content = content
    )
}

/**
 * Glowing neon text displaying modern cyberpunk headers.
 */
@Composable
fun NeonTitle(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Float = 24f,
    glowColor: Color = NeonPurple
) {
    Text(
        text = text,
        color = Color.White,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.drawBehind {
            // Draws secondary subtle text blur or glow shadow base
        }
    )
}

/**
 * Pulsing dynamic equalizing sound wave animation.
 * Features 5 distinct visual bar nodes jumping infinitely.
 */
@Composable
fun AnimatedEqualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    color: Color = NeonBlue,
    barCount: Int = 10,
    barWidth: Dp = 4.dp,
    maxHeight: Dp = 32.dp
) {
    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

        for (i in 0 until barCount) {
            // Speed stagger factor
            val duration = remember { (400..900).random() }
            val animHeightPercent by infiniteTransition.animateFloat(
                initialValue = 0.1f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(duration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )
            val heightPercent = if (isPlaying) animHeightPercent else 0.15f

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(heightPercent)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(HotPink, color)
                        )
                    )
            )
        }
    }
}

/**
 * Floating Emoji Particles Animation.
 * Represents dynamic reactions from peers bubbling upwards.
 */
data class EmojiParticle(
    val id: String,
    val emoji: String,
    val startX: Float,
    val targetY: Float,
    val durationScale: Float,
    val rotation: Float
)

@Composable
fun FloatingEmojiCanvas(
    reactionsFlow: kotlinx.coroutines.flow.SharedFlow<String>,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var particles by remember { mutableStateOf<List<EmojiParticle>>(emptyList()) }

    // Collect and spawn reactions
    LaunchedEffect(reactionsFlow) {
        reactionsFlow.collect { emoji ->
            val randomId = UUID().toString()
            val newParticle = EmojiParticle(
                id = randomId,
                emoji = emoji,
                startX = Random.nextFloat(), // Percent width 0f to 1f
                targetY = Random.nextFloat() * -300f - 100f, // Go up negative coords
                durationScale = Random.nextFloat() * 0.5f + 0.8f, // Speed scale
                rotation = Random.nextFloat() * 60f - 30f // Random tilt
            )
            particles = particles + newParticle

            // Cleanup particle after animation completed
            scope.launch {
                delay(2000)
                particles = particles.filter { it.id != randomId }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            key(particle.id) {
                val progressSpec = tween<Float>(
                    durationMillis = (1500 * particle.durationScale).toInt(),
                    easing = FastOutSlowInEasing
                )

                // Drive vertical translation
                val animProgress = remember { Animatable(0f) }
                LaunchedEffect(particle.id) {
                    animProgress.animateTo(1f, animationSpec = progressSpec)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 30.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(
                                x = (particle.startX * 250).dp,
                                y = (animProgress.value * particle.targetY).dp
                            )
                            .graphicsLayer(
                                alpha = 1f - animProgress.value,
                                scaleX = animProgress.value * 0.8f + 0.4f,
                                scaleY = animProgress.value * 0.8f + 0.4f,
                                rotationZ = particle.rotation
                            )
                    ) {
                        Text(
                            text = particle.emoji,
                            fontSize = 32.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun UUID(): String {
    return (0..99999).random().toString()
}

/**
 * Real-time Sine-wave Canvas Audio Visualizer.
 * Renders elegant synchronized wave crests underneath album artwork.
 * Dynamically reacts to the active song title by adapting frequencies, amplitudes, and custom color gradients.
 */
@Composable
fun AnimatedSineVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    songTitle: String = ""
) {
    val mood = remember(songTitle) {
        val title = songTitle.lowercase()
        when {
            title.contains("acid") || title.contains("edm") || title.contains("techno") || title.contains("remix") || title.contains("synth") || title.contains("groove") -> "energetic"
            title.contains("chill") || title.contains("lofi") || title.contains("dream") || title.contains("ambient") || title.contains("study") -> "lofi"
            title.contains("acoustic") || title.contains("guitar") || title.contains("live") || title.contains("vocal") || title.contains("unplugged") -> "organic"
            else -> "midnight"
        }
    }

    // Adapt speed specs based on music atmosphere
    val durationMillis = when (mood) {
        "energetic" -> 1000
        "lofi" -> 3500
        "organic" -> 2500
        else -> 2000 // midnight
    }

    val infiniteTransition = rememberInfiniteTransition(label = "sine")
    val animatedPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    val phase = if (isPlaying) animatedPhase else 0f

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val wavePath1 = Path()
        val wavePath2 = Path()

        val halfHeight = height / 2f
        
        // Adapt amplitude & frequency specs based on theme
        val baseFreq = when (mood) {
            "energetic" -> 0.04f
            "lofi" -> 0.012f
            "organic" -> 0.018f
            else -> 0.02f // midnight
        }
        
        val baseAmp = when (mood) {
            "energetic" -> if (isPlaying) 42f else 6f
            "lofi" -> if (isPlaying) 12f else 3f
            "organic" -> if (isPlaying) 18f else 4f
            else -> if (isPlaying) 25f else 5f // midnight
        }

        // Color palettes
        val colors1 = when (mood) {
            "energetic" -> listOf(HotPink, Color(0xFFFBBF24)) // hot pink to gold pulse
            "lofi" -> listOf(Color(0xFFFFA07A), Color(0xFFC084FC)) // warm light peach to gentle lavender
            "organic" -> listOf(Color(0xFF34D399), Color(0xFFFBBF24)) // fresh mint green to warm sunny amber
            else -> listOf(NeonPurple, NeonBlue) // midnight classic deep sapphire
        }

        val colors2 = when (mood) {
            "energetic" -> listOf(Color(0xFF22D3EE), NeonPurple) // electric cyan to deep purple
            "lofi" -> listOf(Color(0xFFC084FC), Color(0x33C084FC)) // soft translucent lavender blend
            "organic" -> listOf(Color(0xFFE2E8F0), Color(0xFF34D399)) // slate cloud to clean mint green
            else -> listOf(NeonBlue, HotPink) // midnight cyber fusion
        }

        wavePath1.moveTo(0f, halfHeight)
        wavePath2.moveTo(0f, halfHeight)

        for (x in 0..width.toInt() step 5) {
            val y1 = halfHeight + kotlin.math.sin(x * baseFreq + phase) * baseAmp
            val y2 = halfHeight + kotlin.math.sin(x * baseFreq * 0.7f - phase + 1f) * (baseAmp * 0.8f)
            wavePath1.lineTo(x.toFloat(), y1)
            wavePath2.lineTo(x.toFloat(), y2)
        }

        drawPath(
            path = wavePath1,
            brush = Brush.horizontalGradient(colors1),
            style = Stroke(width = if (mood == "lofi") 2.dp.toPx() else 3.5.dp.toPx())
        )

        drawPath(
            path = wavePath2,
            brush = Brush.horizontalGradient(colors2),
            style = Stroke(width = if (mood == "lofi") 1.2.dp.toPx() else 2.2.dp.toPx(), miter = 4f)
        )
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    color: Color = NeonPurple
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f))
                .border(1.dp, color.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            color = TextSubdued,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(containerColor = color),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(actionText, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun rememberShimmerBrush(
    showShimmer: Boolean = true,
    targetValue: Float = 1000f
): Brush {
    return if (showShimmer) {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnim by transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "translate"
        )
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.08f)
            ),
            start = Offset(translateAnim - 350f, translateAnim - 350f),
            end = Offset(translateAnim, translateAnim)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent)
        )
    }
}

@Composable
fun ShimmerSkeletonCard(
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
    shape: RoundedCornerShape = RoundedCornerShape(14.dp)
) {
    val shimmerBrush = rememberShimmerBrush()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(shimmerBrush)
            .border(1.dp, Color.White.copy(alpha = 0.06f), shape)
    )
}
