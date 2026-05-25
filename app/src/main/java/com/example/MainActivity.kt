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
import com.example.ui.theme.TranslucentSystemDrawer
import com.example.viewmodel.LoopTogetherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: LoopTogetherViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()

                // List of screens where standard bottom navigation bar is active
                val showBottomBar = currentScreen in listOf("home", "explore", "profile")

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = TranslucentSystemDrawer,
                                tonalElevation = 8.dp
                            ) {
                                // Home navigation button
                                NavigationBarItem(
                                    selected = currentScreen == "home",
                                    onClick = { viewModel.navigateTo("home") },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home Hub") },
                                    label = { Text("Home", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = NeonBlue,
                                        selectedTextColor = NeonBlue,
                                        indicatorColor = NeonPurple.copy(alpha = 0.2f),
                                        unselectedIconColor = TextSubdued,
                                        unselectedTextColor = TextSubdued
                                    )
                                )

                                // Search/Explore navigation button
                                NavigationBarItem(
                                    selected = currentScreen == "explore",
                                    onClick = { viewModel.navigateTo("explore") },
                                    icon = { Icon(Icons.Default.Search, contentDescription = "Search & Explore") },
                                    label = { Text("Search", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = NeonBlue,
                                        selectedTextColor = NeonBlue,
                                        indicatorColor = NeonPurple.copy(alpha = 0.2f),
                                        unselectedIconColor = TextSubdued,
                                        unselectedTextColor = TextSubdued
                                    )
                                )

                                // User parameters / profile details button
                                NavigationBarItem(
                                    selected = currentScreen == "profile",
                                    onClick = { viewModel.navigateTo("profile") },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Personal Profile") },
                                    label = { Text("Profile", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = NeonBlue,
                                        selectedTextColor = NeonBlue,
                                        indicatorColor = NeonPurple.copy(alpha = 0.2f),
                                        unselectedIconColor = TextSubdued,
                                        unselectedTextColor = TextSubdued
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    val activeRoom by viewModel.activeRoom.collectAsState()
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
                                                    NeonPurple.copy(alpha = 0.3f),
                                                    NeonBlue.copy(alpha = 0.3f)
                                                )
                                            ),
                                            RoundedCornerShape(32.dp)
                                        )
                                        .clickable { viewModel.navigateTo("room") },
                                    shape = RoundedCornerShape(32.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xF209090E))
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
                                                .border(1.5.dp, NeonBlue, RoundedCornerShape(24.dp))
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
                                                    .background(Color(0xFF020205))
                                                    .border(1.dp, NeonBlue, RoundedCornerShape(5.dp))
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Song Details
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = room.currentSongTitle,
                                                color = Color.White,
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
                                                    color = TextSubdued,
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
                                                    Brush.linearGradient(listOf(NeonPurple, NeonBlue)),
                                                    RoundedCornerShape(19.dp)
                                                )
                                        ) {
                                            Icon(
                                                imageVector = if (room.isPlaying) androidx.compose.material.icons.Icons.Default.Pause else androidx.compose.material.icons.Icons.Default.PlayArrow,
                                                contentDescription = "Playback Control",
                                                tint = Color.White,
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
