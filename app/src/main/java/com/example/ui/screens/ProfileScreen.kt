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
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = user?.email ?: "loop.user@example.com",
                            fontSize = 11.sp,
                            color = TextSubdued
                        )
                        Spacer(modifier = Modifier.height(4.dp))
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
                            text = "${user?.listeningMinutes ?: 1420}",
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
                            text = "24",
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

            // Diagnostic configurations and settings
            item {
                Text(
                    text = "Audio Diagnostics & Settings",
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
                        // Toggle Audio visualizer
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Aero-Sine Audio Visualizer", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Perform high-contrast waveform calculations", color = TextSubdued, fontSize = 10.sp)
                            }
                            Switch(
                                checked = visualizerOn,
                                onCheckedChange = { viewModel.isVisualizerEnabled.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonBlue)
                            )
                        }

                        // Toggle Biometric verification simulation
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Wired Hardware Output Optimization", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Optimize local buffer sizes for lowest latency", color = TextSubdued, fontSize = 10.sp)
                            }
                            Switch(
                                checked = wireOn,
                                onCheckedChange = { viewModel.isAudioOutputWired.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonBlue)
                            )
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
