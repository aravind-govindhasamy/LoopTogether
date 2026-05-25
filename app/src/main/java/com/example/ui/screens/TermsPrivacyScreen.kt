package com.example.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlowCard
import com.example.ui.components.frostedGlassBackground
import com.example.ui.theme.*
import com.example.viewmodel.LoopTogetherViewModel

@Composable
fun TermsPrivacyScreen(viewModel: LoopTogetherViewModel) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Terms, 1 = Privacy, 2 = Guidelines

    Box(
        modifier = Modifier
            .fillMaxSize()
            .frostedGlassBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
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
                    text = "Legal & Code of Vibe",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Beautiful glass segmented control tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSpaceSurface)
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val tabTitles = listOf("Terms of Service", "Privacy Policy", "Community")
                tabTitles.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.background(Brush.linearGradient(listOf(NeonPurple, NeonBlue)))
                                } else {
                                    Modifier.background(Color.Transparent)
                                }
                            )
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) Color.White else TextSubdued,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Content Area based on selected Tab
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    0 -> { // TERMS OF SERVICE
                        item {
                            LegalIntroCard(
                                title = "Terms of Service Agreement",
                                updateDate = "Last updated: May 2026",
                                text = "Welcome to LoopTogether. These terms outline user responsibilities, acceptable streaming behaviors, and dynamic account covenants."
                            )
                        }

                        item {
                            LegalSectionCard(
                                number = "1",
                                title = "User Account Covenants",
                                detailedText = "By joining LoopTogether nodes via Google Credentials, users agree to safeguard their local account tokens. All activities executed within custom dynamic tunnels are assigned to the primary host. The platform reserves the right to invalidate active local cache sessions for malicious loop operations."
                            )
                        }

                        item {
                            LegalSectionCard(
                                number = "2",
                                title = "Acceptable Streaming & IP",
                                detailedText = "LoopTogether operates exclusively as a real-time playback synchronization engine bridging public streaming endpoints (primarily YouTube APIs). Users are strictly prohibited from engineering stream-ripping routines, copying decrypted packets, or engaging in copyright infringement. All streamed assets remain the intellectual property of original artists and content providers."
                            )
                        }

                        item {
                            LegalSectionCard(
                                number = "3",
                                title = "Tunnel Behavior & Moderation",
                                detailedText = "Room Hosts are granted full moderation capabilities including song queue manipulation, member expulsion, and public/private adjustments. Disruptive queue practices, chat blockages, or spamming API networks will trigger automatic disconnect watchdogs."
                            )
                        }

                        item {
                            LegalSectionCard(
                                number = "4",
                                title = "Service Limitations & Drift Support",
                                detailedText = "Because remote audio synchronization operates over multi-hop web links, playback offset tolerances are dependent on local round-trip times. LoopTogether performs dynamic, client-side linear clock drift calibrations but does not guarantee zero latency under highly saturated network configurations."
                            )
                        }
                    }
                    1 -> { // PRIVACY POLICY
                        item {
                            LegalIntroCard(
                                title = "Privacy & Safety Policy",
                                updateDate = "Last updated: May 2026",
                                text = "Your musical frequencies belong to you. We describe how securely we process identities, room caches, and chat transcripts below."
                            )
                        }

                        item {
                            LegalSectionCard(
                                number = "A",
                                title = "Minimal Datastore Collection",
                                detailedText = "LoopTogether collects basic, essential descriptors to render your active looper avatar: Username representation, email nodes, and temporary profiles pic emojis. We DO NOT compile, harvest, or monetize catalogs of your favorite artists or listening histories."
                            )
                        }

                        item {
                            LegalSectionCard(
                                number = "B",
                                title = "OAuth Node Integration",
                                detailedText = "Google Node authentication credentials are handled locally or via secure OAuth handshake processes. Your password blocks are never transmitted, seen, or logged by LoopTogether. Verification tokens are encrypted directly within Room Database tables."
                            )
                        }

                        item {
                            LegalSectionCard(
                                number = "C",
                                title = "Session & Chat Purging",
                                detailedText = "Active conversations, sent emojis, and shared tracks are cached locally to support full-duplex room connections. Upon tunnel destruction or room closure, chat sequences are marked for deletion from active states and completely garbage-collected."
                            )
                        }

                        item {
                            LegalSectionCard(
                                number = "D",
                                title = "Anonymized Performance Telemetry",
                                detailedText = "We analyze anonymized network latency markers, clock offset adjustments, and airtime counts. No identifiable credentials are attached, ensuring our low-latency scaling algorithms remain entirely private."
                            )
                        }
                    }
                    2 -> { // COMMUNITY GUIDELINES
                        item {
                            GlowCard(
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = ActiveGreen,
                                hasGlow = true
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = ActiveGreen, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Code of Frequencies", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("Maintain clean vibes and harmonic sync", color = TextSubdued, fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                "Core Vibe Expectations",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                GuidelineRuleCard(
                                    emoji = "🤝",
                                    title = "Respect Room Creators & Hosts",
                                    desc = "Hosts set the direction of listening tunnels. Support the vibe or politely search/create another active room."
                                )
                                GuidelineRuleCard(
                                    emoji = "🛡️",
                                    title = "Zero Tolerance for Abuse & Hate",
                                    desc = "Harassment, slur deployment, or spamming inside public chat streams will yield immediate, irreversible session banishment."
                                )
                                GuidelineRuleCard(
                                    emoji = "🎸",
                                    title = "Share Queue Airtime Equitably",
                                    desc = "Avoid mass-skipping or spam-adding tracks to the request pool without talking first in the room chat."
                                )
                                GuidelineRuleCard(
                                    emoji = "🔊",
                                    title = "Calibration Integrity",
                                    desc = "Please calibrate your latency settings correctly in preferences to secure harmonic synchronization."
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegalIntroCard(
    title: String,
    updateDate: String,
    text: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSpaceSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Text(updateDate, color = NeonPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text, color = TextSubdued, fontSize = 11.5.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
fun LegalSectionCard(
    number: String,
    title: String,
    detailedText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(NeonBlue.copy(alpha = 0.15f))
                .border(1.dp, NeonBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = detailedText,
                color = TextSubdued,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun GuidelineRuleCard(
    emoji: String,
    title: String,
    desc: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSpaceSurface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(desc, color = TextSubdued, fontSize = 10.sp, lineHeight = 14.sp)
            }
        }
    }
}
