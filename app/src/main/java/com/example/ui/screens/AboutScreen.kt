package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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

@Composable
fun AboutScreen(viewModel: LoopTogetherViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .frostedGlassBackground()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header back bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateTo("profile") },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DarkSpaceSurface)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "About LoopTogether",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Hero Section
            item {
                val infiniteTransition = rememberInfiniteTransition(label = "about_logo")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.98f,
                    targetValue = 1.02f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                GlowCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    borderColor = NeonPurple,
                    hasGlow = true
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(NeonPurple, NeonBlue))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "LOOP TOGETHER",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = "Shared Frequencies • Real-time Harmony",
                            fontSize = 12.sp,
                            color = NeonBlue,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "LoopTogether brings people closer through shared music, synchronized experiences, and real-time human connection.",
                            fontSize = 13.sp,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }

            // Our Vision
            item {
                Text(
                    text = "Our Core Vision",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSpaceSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Music is a Social Experience", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text(
                            text = "We believe that social listening is the ultimate future of music interaction. Listening to your favorite artists alone is therapeutic, but sharing standard playlists synchronously with your loved ones, studying partners, or long-distance coordinates creates magic.",
                            fontSize = 12.sp,
                            color = TextSubdued,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Features cards overview
            item {
                Text(
                    text = "Synchronized Architecture Features",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AboutFeatureRow(
                        icon = Icons.Default.Sync,
                        title = "Millisecond Sync Accuracy",
                        description = "State-of-the-art clock drift calibration locks devices within <100ms. No playback lag.",
                        color = NeonBlue
                    )
                    AboutFeatureRow(
                        icon = Icons.Default.LiveTv,
                        title = "YouTube Backed Streaming",
                        description = "Access billions of tracks and live video sessions synchronously in the listening tunnels.",
                        color = HotPink
                    )
                    AboutFeatureRow(
                        icon = Icons.Default.Sms,
                        title = "Full-Duplex Interactive Chat",
                        description = "Express vibes instantly via emoji explosions, typing indicator lights, and live feed tickers.",
                        color = NeonPurple
                    )
                }
            }

            // Built For audience categorization
            item {
                Text(
                    text = "Engineered & Custom-Built For",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .background(DarkSpaceSurface)
                            .padding(12.dp)
                    ) {
                        Column {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = HotPink, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Couples & LDRs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Stay emotionally sync'd.", fontSize = 9.sp, color = TextSubdued)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .background(DarkSpaceSurface)
                            .padding(12.dp)
                    ) {
                        Column {
                            Icon(Icons.Default.Group, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Friends & Crews", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Vibe under a joint room.", fontSize = 9.sp, color = TextSubdued)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .background(DarkSpaceSurface)
                            .padding(12.dp)
                    ) {
                        Column {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Study/Co-working", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Lofi streams together.", fontSize = 9.sp, color = TextSubdued)
                        }
                    }
                }
            }

            // Technology Badges Section
            item {
                Text(
                    text = "Startup Technology Stack",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSpaceSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ROBUST DISTRIBUTED INFRASTRUCTURE", fontSize = 9.sp, color = TextSubdued, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TechBadge("Compose 🎨")
                            TechBadge("Socket.io 📡")
                            TechBadge("Room DB 🗄️")
                            TechBadge("Kotlin DSL 🤖")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TechBadge("YouTube REST API 📈")
                            TechBadge("Coroutines Flow ⏳")
                            TechBadge("Biometrics Lock 🔐")
                        }
                    }
                }
            }

            // Footer info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LoopTogether v1.4.0 • Enterprise Rated Space",
                        fontSize = 10.sp,
                        color = TextSubdued,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "© 2026 LoopTogether Inc. All theoretical rights reserved.",
                        fontSize = 9.sp,
                        color = TextSubdued.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun AboutFeatureRow(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSpaceSurface)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(description, color = TextSubdued, fontSize = 10.sp, lineHeight = 14.sp)
        }
    }
}

@Composable
fun TechBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium)
    }
}
