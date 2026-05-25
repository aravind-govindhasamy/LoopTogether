package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.LoopTogetherViewModel
import kotlinx.coroutines.delay

import com.example.ui.components.frostedGlassBackground

@Composable
fun SplashScreen(viewModel: LoopTogetherViewModel) {
    val scaleAnim = remember { Animatable(0.5f) }
    val glowAnim = rememberInfiniteTransition(label = "glow_pulse")

    val pulseScale by glowAnim.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Trigger Entrance animation
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        // Hold on splash for 2.2 seconds before moving
        delay(2200)
        if (viewModel.currentUser.value != null) {
            viewModel.navigateTo("home")
        } else {
            viewModel.navigateTo("login")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .frostedGlassBackground(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scaleAnim.value * pulseScale)
        ) {
            // Spinning Glow Ring
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = CircleShape,
                        ambientColor = NeonBlue,
                        spotColor = NeonPurple
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NeonPurple, NeonBlue, HotPink)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "LoopTogether Core",
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Premium Futuristic Logo Typography
            Text(
                text = "LOOP",
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 4.sp
            )

            Text(
                text = "TOGETHER",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = NeonBlue,
                letterSpacing = 8.sp,
                modifier = Modifier.offset(x = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Real-time Synchronized Space",
                fontSize = 12.sp,
                color = TextSubdued,
                letterSpacing = 2.sp
            )
        }

        // Bottom Loading / Attribution indicator
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SYNCHRONIZING AUDIO ENGINE",
                    fontSize = 10.sp,
                    color = NeonBlue.copy(alpha = 0.8f),
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sync delay rated: <100ms",
                    fontSize = 10.sp,
                    color = TextSubdued.copy(alpha = 0.6f)
                )
            }
        }
    }
}
