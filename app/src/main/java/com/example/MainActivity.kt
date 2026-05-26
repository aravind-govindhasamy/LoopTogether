package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextSubdued
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer
import com.example.viewmodel.LoopTogetherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LoopTogetherViewModel = viewModel()
            val activeTheme by viewModel.appTheme.collectAsState()
            val activeRoom by viewModel.activeRoom.collectAsState()
            val songTitle = activeRoom?.currentSongTitle ?: ""

            MyApplicationTheme(theme = activeTheme, songTitle = songTitle) {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val loopColors = com.example.ui.theme.LocalLoopColors.current

                // List of screens where standard bottom navigation bar is active
                val showBottomBar = currentScreen in listOf("home", "explore", "profile")

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .padding(bottom = 12.dp)
                                    .navigationBarsPadding(),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(32.dp))
                                        .background(loopColors.background.copy(alpha = 0.85f))
                                        .border(
                                            1.dp,
                                            Brush.horizontalGradient(
                                                listOf(
                                                    loopColors.primary.copy(alpha = 0.25f),
                                                    loopColors.secondary.copy(alpha = 0.15f)
                                                )
                                            ),
                                            RoundedCornerShape(32.dp)
                                        )
                                        .padding(vertical = 6.dp, horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Custom beautifully animated home navigation button
                                    val isHome = currentScreen == "home"
                                    val homeScale by animateFloatAsState(if (isHome) 1.12f else 1.0f, label = "home_scale")
                                    Column(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { viewModel.navigateTo("home") }
                                            .padding(vertical = 6.dp, horizontal = 12.dp)
                                            .graphicsLayer(scaleX = homeScale, scaleY = homeScale),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (isHome) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(loopColors.primary.copy(alpha = 0.15f))
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.Home,
                                                contentDescription = "Home Hub",
                                                tint = if (isHome) loopColors.primary else loopColors.textSubdued,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "Home",
                                            fontSize = 9.sp,
                                            fontWeight = if (isHome) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isHome) loopColors.primary else loopColors.textSubdued
                                        )
                                    }

                                    // Search/Explore navigation button
                                    val isExplore = currentScreen == "explore"
                                    val exploreScale by animateFloatAsState(if (isExplore) 1.12f else 1.0f, label = "exp_scale")
                                    Column(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { viewModel.navigateTo("explore") }
                                            .padding(vertical = 6.dp, horizontal = 12.dp)
                                            .graphicsLayer(scaleX = exploreScale, scaleY = exploreScale),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (isExplore) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(loopColors.primary.copy(alpha = 0.15f))
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Search Catalogue",
                                                tint = if (isExplore) loopColors.primary else loopColors.textSubdued,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "Search",
                                            fontSize = 9.sp,
                                            fontWeight = if (isExplore) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isExplore) loopColors.primary else loopColors.textSubdued
                                        )
                                    }

                                    // Profile navigation button
                                    val isProfile = currentScreen == "profile"
                                    val profileScale by animateFloatAsState(if (isProfile) 1.12f else 1.0f, label = "prof_scale")
                                    Column(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { viewModel.navigateTo("profile") }
                                            .padding(vertical = 6.dp, horizontal = 12.dp)
                                            .graphicsLayer(scaleX = profileScale, scaleY = profileScale),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (isProfile) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(loopColors.primary.copy(alpha = 0.15f))
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Personal Profile",
                                                tint = if (isProfile) loopColors.primary else loopColors.textSubdued,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "Profile",
                                            fontSize = 9.sp,
                                            fontWeight = if (isProfile) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isProfile) loopColors.primary else loopColors.textSubdued
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    val showMiniPlayer = activeRoom != null && currentScreen in listOf("home", "explore", "profile")

                    // Slow rotation for mini-player artwork
                    val infiniteTransition = rememberInfiniteTransition(label = "mini_vinyl")
                    val miniRotationAngle by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(14000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "mini_angle"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        // Main Screens Container
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() + (if (showMiniPlayer) 75.dp else 0.dp) else 0.dp)
                        ) {
                            when (currentScreen) {
                                "splash" -> SplashScreen(viewModel = viewModel)
                                "login" -> LoginScreen(viewModel = viewModel)
                                "home" -> HomeScreen(viewModel = viewModel)
                                "explore" -> ExploreScreen(viewModel = viewModel)
                                "room" -> RoomScreen(viewModel = viewModel)
                                "profile" -> ProfileScreen(viewModel = viewModel)
                                "settings" -> SettingsScreen(viewModel = viewModel)
                                "support" -> SupportScreen(viewModel = viewModel)
                                "contact" -> ContactScreen(viewModel = viewModel)
                                "about" -> AboutScreen(viewModel = viewModel)
                                "terms" -> TermsPrivacyScreen(viewModel = viewModel)
                                "onboarding" -> OnboardingScreen(viewModel = viewModel)
                            }
                        }

                        // Premium slide-in Global Mini Player Overlay
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showMiniPlayer,
                            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + androidx.compose.animation.fadeIn(),
                            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + androidx.compose.animation.fadeOut(),
                            modifier = Modifier
                                .align(androidx.compose.ui.Alignment.BottomCenter)
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = if (showBottomBar) innerPadding.calculateBottomPadding() + 8.dp else 16.dp
                                )
                        ) {
                            activeRoom?.let { room ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp)
                                        .border(
                                            1.dp,
                                            Brush.horizontalGradient(
                                                listOf(
                                                    loopColors.primary.copy(alpha = 0.4f),
                                                    loopColors.secondary.copy(alpha = 0.4f)
                                                )
                                            ),
                                            RoundedCornerShape(32.dp)
                                        )
                                        .clickable { viewModel.navigateTo("room") },
                                    shape = RoundedCornerShape(32.dp),
                                    colors = CardDefaults.cardColors(containerColor = loopColors.background.copy(alpha = 0.92f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(start = 8.dp, end = 16.dp),
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                    ) {
                                        // Rotating Vinyl
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(24.dp))
                                                .border(1.5.dp, loopColors.secondary, RoundedCornerShape(24.dp))
                                        ) {
                                            coil.compose.AsyncImage(
                                                model = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=150&auto=format&fit=crop",
                                                contentDescription = "Song artwork",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .rotate(if (room.isPlaying) miniRotationAngle else 0f)
                                            )
                                            // Center pinhole
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .align(androidx.compose.ui.Alignment.Center)
                                                    .clip(RoundedCornerShape(5.dp))
                                                    .background(loopColors.background)
                                                    .border(1.dp, loopColors.secondary, RoundedCornerShape(5.dp))
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Song Details
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = room.currentSongTitle,
                                                color = loopColors.textPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Row(
                                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(RoundedCornerShape(2.5.dp))
                                                        .background(com.example.ui.theme.ActiveGreen)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${room.currentSongArtist} • Dynamic Tunnel",
                                                    color = loopColors.textSubdued,
                                                    fontSize = 11.sp,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Mini controller Play/Pause
                                        IconButton(
                                            onClick = { viewModel.togglePlaybackState() },
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(
                                                    Brush.linearGradient(listOf(loopColors.primary, loopColors.secondary)),
                                                    RoundedCornerShape(19.dp)
                                                )
                                        ) {
                                            Icon(
                                                imageVector = if (room.isPlaying) androidx.compose.material.icons.Icons.Default.Pause else androidx.compose.material.icons.Icons.Default.PlayArrow,
                                                contentDescription = "Playback Control",
                                                tint = if (loopColors.isDark) Color.White else loopColors.background,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
