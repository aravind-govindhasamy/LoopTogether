package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlowCard
import com.example.ui.theme.*
import com.example.viewmodel.LoopTogetherViewModel

import com.example.ui.components.frostedGlassBackground

@Composable
fun ProfileScreen(viewModel: LoopTogetherViewModel) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val availableRooms by viewModel.availableRooms.collectAsState()

    var editUsername by remember { mutableStateOf(user?.username ?: "") }
    var editEmail by remember { mutableStateOf(user?.email ?: "") }
    var selectedAv by remember { mutableStateOf(user?.profilePicUrl ?: "🎧") }

    val avatarList = listOf("🎧", "🔥", "⚡", "👾", "🦊", "🎸", "🌌")

    // Diagnostics Switches
    val visualizerOn by viewModel.isVisualizerEnabled.collectAsState()
    val wireOn by viewModel.isAudioOutputWired.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .frostedGlassBackground()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 80.dp),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Profile Banner
            item {
                GlowCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NeonBlue,
                    hasGlow = true
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(NeonPurple, NeonBlue))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = selectedAv, fontSize = 42.sp)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = user?.username ?: "SoundWave_Explorer",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = user?.email ?: "loop.user@example.com",
                                fontSize = 11.sp,
                                color = TextSubdued
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeonBlue.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "VIP LOOPER • LEVEL 7",
                                        color = NeonBlue,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(HotPink.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "COSMIC AURA 🔮",
                                        color = HotPink,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Music personality vibe tags
                    Text("PERSONAL MUSIC CORE", fontSize = 9.sp, color = TextSubdued, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Synthwave ⚡", "Lofi Sunset 🌆", "Liquidity DnB 🌊", "Unplugged 🎸").forEach { vibe ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(vibe, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Stat Board Block
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Minutes listened Card
                    GlowCard(
                        modifier = Modifier.weight(1f),
                        borderColor = NeonPurple,
                        hasGlow = false
                    ) {
                        Text("Mins Airtime", fontSize = 11.sp, color = TextSubdued)
                        Text(
                            text = "${user?.listeningMinutes ?: 0}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text("Synchronised tracks", fontSize = 9.sp, color = NeonPurple)
                    }

                    // Rooms explored Card
                    GlowCard(
                        modifier = Modifier.weight(1f),
                        borderColor = NeonBlue,
                        hasGlow = false
                    ) {
                        Text("Active Tunnels", fontSize = 11.sp, color = TextSubdued)
                        Text(
                            text = "${availableRooms.size}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text("Total join events", fontSize = 9.sp, color = NeonBlue)
                    }
                }
            }

            // Interactive Editor Section
            item {
                Text(
                    text = "Edit Cosmic Identity",
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
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSpaceSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = editUsername,
                            onValueChange = { editUsername = it },
                            label = { Text("Display Tag", fontSize = 11.sp) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, focusedBorderColor = NeonPurple
                            )
                        )

                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("Google Node Email", fontSize = 11.sp) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, focusedBorderColor = NeonPurple
                            )
                        )

                        Text("Choose Emblem", fontSize = 11.sp, color = TextSubdued, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            avatarList.forEach { av ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedAv == av) NeonPurple.copy(alpha = 0.3f) else Color.Transparent)
                                        .border(1.dp, if (selectedAv == av) NeonPurple else Color.Transparent, CircleShape)
                                        .clickable { selectedAv = av },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = av, fontSize = 16.sp)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.updateProfile(editUsername, editEmail, selectedAv)
                                Toast.makeText(context, "Cosmic profile updated!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Confirm Adaptations", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Notification board Header
            item {
                Text(
                    text = "Active Signal Notifications (${notifications.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (notifications.isEmpty()) {
                item {
                    Text(
                        "No alarms or pending listening invites.",
                        color = TextSubdued,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                items(notifications) { notification ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSpaceSurface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (notification.type) {
                                    "INVITE" -> Icons.Default.MailOutline
                                    else -> Icons.Default.Notifications
                                },
                                contentDescription = null,
                                tint = NeonBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notification.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = notification.description,
                                    fontSize = 10.sp,
                                    color = TextSubdued
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteNotificationItem(notification.id) }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss Notification", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Trust, Support and Legal Navigation Menus
            item {
                Text(
                    text = "Application Navigation Hub",
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
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSpaceSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Settings
                        ProfileNavigationRow(
                            icon = Icons.Default.Settings,
                            title = "App Settings preferences",
                            subtitle = "Sync latency, HD audio, notifications & block lists",
                            color = NeonBlue
                        ) {
                            viewModel.navigateTo("settings")
                        }

                        Divider(color = Color.White.copy(alpha = 0.06f))

                        // 2. Help FAQ
                        ProfileNavigationRow(
                            icon = Icons.Default.HelpCenter,
                            title = "Help Center & FAQ",
                            subtitle = "Interactive troubleshooting accordion & bug reports",
                            color = NeonPurple
                        ) {
                            viewModel.navigateTo("support")
                        }

                        Divider(color = Color.White.copy(alpha = 0.06f))

                        // 3. Contact Inquiries
                        ProfileNavigationRow(
                            icon = Icons.Default.ContactSupport,
                            title = "Inquiries & Core Feedback",
                            subtitle = "Support desks, business proposals, server logs",
                            color = HotPink
                        ) {
                            viewModel.navigateTo("contact")
                        }

                        Divider(color = Color.White.copy(alpha = 0.06f))

                        // 4. About
                        ProfileNavigationRow(
                            icon = Icons.Default.Info,
                            title = "About LoopTogether",
                            subtitle = "Product startup blueprint vision & tech badges",
                            color = ActiveGreen
                        ) {
                            viewModel.navigateTo("about")
                        }

                        Divider(color = Color.White.copy(alpha = 0.06f))

                        // 5. Terms / Privacy
                        ProfileNavigationRow(
                            icon = Icons.Default.Gavel,
                            title = "Legal Covenants & Code of Vibe",
                            subtitle = "Terms, user data safety, community guidelines",
                            color = Color.White.copy(alpha = 0.7f)
                        ) {
                            viewModel.navigateTo("terms")
                        }
                    }
                }
            }

            // Session logging out controls
            item {
                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Invalidate Google Session (Logout)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfileNavigationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextSubdued, fontSize = 10.sp)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSubdued,
            modifier = Modifier.size(18.dp)
        )
    }
}
