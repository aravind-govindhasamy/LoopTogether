package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlowCard
import com.example.ui.components.frostedGlassBackground
import com.example.ui.theme.*
import com.example.viewmodel.LoopTogetherViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(viewModel: LoopTogetherViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 5 })
    
    val slides = listOf(
        OnboardingSlide(
            title = "Welcome to LoopTogether",
            subtitle = "SHARED FREQUENCIES",
            description = "LoopTogether brings people closer through shared music, synchronized playback, and instant emotional connections.",
            icon = Icons.Default.MusicNote,
            color = NeonPurple
        ),
        OnboardingSlide(
            title = "Perfect Listener Sync",
            subtitle = "MILLISECOND ALIGNED",
            description = "Experience flawless high-performance audio synchronization under <100ms. No lag, no latency, just continuous harmony.",
            icon = Icons.Default.PlayArrow,
            color = NeonBlue
        ),
        OnboardingSlide(
            title = "Watch Synchronized Videos",
            subtitle = "YOUTUBE BACKED",
            description = "Stream your favorite YouTube tunes and watch videos concurrently with friends and couples as if you're on the same couch.",
            icon = Icons.Default.Tv,
            color = HotPink
        ),
        OnboardingSlide(
            title = "Reactive Chat & Reactions",
            subtitle = "EXPRESS IN REAL TIME",
            description = "Send interactive emoji explosions, type in live chat channels, and receive peer visual actions instantly.",
            icon = Icons.Default.Search,
            color = ActiveGreen
        ),
        OnboardingSlide(
            title = "Create Your First Room",
            subtitle = "JOIN THE TUNNEL",
            description = "Hosting is immediate. Claim a live tunnel code, invite your partner, study crew, or long-distance buddy, and start looping!",
            icon = Icons.Default.MusicNote,
            color = NeonPurple
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .frostedGlassBackground()
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar with Skip Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(NeonPurple, NeonBlue))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "LOOP",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                }

                Text(
                    text = "Skip",
                    fontSize = 13.sp,
                    color = TextSubdued,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        viewModel.onboardingCompleted.value = true
                        viewModel.navigateTo("login")
                    }
                )
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val slide = slides[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Floating Animated Emblem
                    val infiniteTransition = rememberInfiniteTransition(label = "slide_opt")
                    val scaleFactor by infiniteTransition.animateFloat(
                        initialValue = 0.96f,
                        targetValue = 1.04f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(scaleFactor)
                            .clip(CircleShape)
                            .background(slide.color.copy(alpha = 0.12f))
                            .border(1.5.dp, slide.color.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(slide.color, Color.Transparent))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = slide.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Text Grouping
                    Text(
                        text = slide.subtitle,
                        color = slide.color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = slide.title,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = slide.description,
                        color = TextSubdued,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Bottom Navigation & Indicators
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        val selected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (selected) 24.dp else 8.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(if (selected) NeonBlue else Color.White.copy(alpha = 0.2f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Primary Blue Action Button
                val isLastPage = pagerState.currentPage == 4
                Button(
                    onClick = {
                        if (isLastPage) {
                            viewModel.onboardingCompleted.value = true
                            viewModel.navigateTo("login")
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLastPage) ActiveGreen else NeonBlue
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isLastPage) "Get Started 💫" else "Continue",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        if (!isLastPage) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Footer brand info
                Text(
                    text = "LoopTogether v1.4.0 • Crafted for Pure Harmony",
                    color = TextSubdued.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)
