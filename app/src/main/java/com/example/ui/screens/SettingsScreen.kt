package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlowCard
import com.example.ui.components.frostedGlassBackground
import com.example.ui.theme.*
import com.example.viewmodel.LoopTogetherViewModel

@Composable
fun SettingsScreen(viewModel: LoopTogetherViewModel) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()

    // Retrieve state variables from ViewModel to keep them fully reactive!
    val latencyMs by viewModel.syncLatencyMs.collectAsState()
    val visualizerEnabled by viewModel.isVisualizerEnabled.collectAsState()
    val audioOutputWired by viewModel.isAudioOutputWired.collectAsState()

    val audioQual by viewModel.audioQuality.collectAsState()
    val videoQual by viewModel.videoQuality.collectAsState()
    
    val pushOn by viewModel.pushNotificationsEnabled.collectAsState()
    val friendActivityOn by viewModel.friendActivityAlertsEnabled.collectAsState()
    val invitesOn by viewModel.roomInvitesEnabled.collectAsState()
    
    val midnightThemeOn by viewModel.cosmicMidnightTheme.collectAsState()
    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val reducedMotionOn by viewModel.reducedMotionEnabled.collectAsState()
    
    val blockedUsersList by viewModel.blockedUsers.collectAsState()
    val visibilityMode by viewModel.profileVisibility.collectAsState()
    val showActivityOn by viewModel.showActivityStatus.collectAsState()

    var customBlockInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .frostedGlassBackground()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 12.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
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
                        text = "App Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // --- SECTION: ACCOUNT COVENANTS ---
            item {
                SettingsSectionHeader(title = "Account Settings")
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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // User mini identity card
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(NeonPurple, NeonBlue))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(user?.profilePicUrl ?: "🎧", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(user?.username ?: "Anonymous Looper", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(user?.email ?: "looper@looptogether.io", color = TextSubdued, fontSize = 10.sp)
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.06f))

                        // Session Cleansing logs
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.deleteHistoryLogs()
                                }
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Purge Playback History Logs", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("Clean up localized SQLite tracks cache", color = TextSubdued, fontSize = 10.sp)
                            }
                        }

                        // Danger Zone deletion request
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.requestAccountDeletion()
                                    Toast.makeText(context, "Account scheduled for removal.", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Request Account Deletion", color = Color.Red.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Permanent purge from LoopTogether nodes", color = TextSubdued, fontSize = 10.sp)
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.logout()
                                Toast.makeText(context, "Logged out of current node", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Log Out Session", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            // --- SECTION: PLAYBACK SYNC CALIBRATION ---
            item {
                SettingsSectionHeader(title = "Playback Alignment & Quality")
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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Latency offset calibration slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Sync Calibration Offset", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Client-side latency adjustment limit (0 - 500ms)", color = TextSubdued, fontSize = 10.sp)
                                }
                                Text("${latencyMs}ms", color = NeonBlue, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Slider(
                                value = latencyMs.toFloat(),
                                onValueChange = { viewModel.syncLatencyMs.value = it.toInt() },
                                valueRange = 0f..500f,
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonBlue,
                                    activeTrackColor = NeonBlue,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                                )
                            )
                        }

                        Divider(color = Color.White.copy(alpha = 0.06f))

                        // Audio quality nodes choice
                        Column {
                            Text("Lossless Audio Quality Pool", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("standard" to "Standard (128k)", "high" to "HD Dynamic (320k)", "epic" to "Cosmic Lossless 🔮").forEach { (id, label) ->
                                    val isSel = audioQual == id
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) NeonPurple.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f))
                                            .border(1.dp, if (isSel) NeonPurple else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.audioQuality.value = id }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.06f))

                        // Video quality configurations Choice
                        Column {
                            Text("YouTube Video Stream Quality", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("standard" to "360p Compact", "high" to "1080p Web Stream", "auto" to "Adaptive Live").forEach { (id, label) ->
                                    val isSel = videoQual == id
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) NeonBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f))
                                            .border(1.dp, if (isSel) NeonBlue else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.videoQuality.value = id }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- SECTION: NOTIFICATIONS ---
            item {
                SettingsSectionHeader(title = "Push Signal Notifications")
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsToggleRow(
                            title = "Enable Push Notifications",
                            subtitle = "Show background notifications alert triggers",
                            checked = pushOn,
                            onCheckedChange = { viewModel.pushNotificationsEnabled.value = it }
                        )
                        Divider(color = Color.White.copy(alpha = 0.06f))
                        SettingsToggleRow(
                            title = "Friend Activity Signals",
                            subtitle = "Alert me when couples or friends start looping",
                            checked = friendActivityOn,
                            onCheckedChange = { viewModel.friendActivityAlertsEnabled.value = it }
                        )
                        Divider(color = Color.White.copy(alpha = 0.06f))
                        SettingsToggleRow(
                            title = "Tunnel Room Invites",
                            subtitle = "Receive alert pings for direct member entries",
                            checked = invitesOn,
                            onCheckedChange = { viewModel.roomInvitesEnabled.value = it }
                        )
                    }
                }
            }

            // --- SECTION: APPEARANCE ENGINE ---
            item {
                SettingsSectionHeader(title = "Cosmic Theme & Appearance")
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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SettingsToggleRow(
                            title = "Deep Cosmic Midnight Theme",
                            subtitle = "Replaces classic dark with pure high-contrast velvet black",
                            checked = midnightThemeOn,
                            onCheckedChange = { viewModel.cosmicMidnightTheme.value = it }
                        )

                        Divider(color = Color.White.copy(alpha = 0.06f))

                        Column {
                            Text("System Core Glow Color Accent", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Adapt core cards, glowing rings, and button nodes", color = TextSubdued, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                AccentCircle(colorName = "purple", brush = Brush.sweepGradient(listOf(NeonPurple, NeonBlue, NeonPurple)), current = accentColorIndex) {
                                    viewModel.accentColorIndex.value = "purple"
                                }
                                AccentCircle(colorName = "blue", brush = Brush.sweepGradient(listOf(NeonBlue, Color.Cyan, NeonBlue)), current = accentColorIndex) {
                                    viewModel.accentColorIndex.value = "blue"
                                }
                                AccentCircle(colorName = "pink", brush = Brush.sweepGradient(listOf(HotPink, NeonPurple, HotPink)), current = accentColorIndex) {
                                    viewModel.accentColorIndex.value = "pink"
                                }
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.06f))

                        SettingsToggleRow(
                            title = "Reduced Motion Mode",
                            subtitle = "Disables heavy rotations and complex wave canvases",
                            checked = reducedMotionOn,
                            onCheckedChange = { viewModel.reducedMotionEnabled.value = it }
                        )
                    }
                }
            }

            // --- SECTION: PRIVACY NODES & BLOCKS ---
            item {
                SettingsSectionHeader(title = "Privacy & Block Gateways")
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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column {
                            Text("Profile Listing Visibility", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Public", "Friends Only", "Private").forEach { mode ->
                                    val isSel = visibilityMode == mode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) NeonBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f))
                                            .border(1.dp, if (isSel) NeonBlue else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.profileVisibility.value = mode }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(mode, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.06f))

                        SettingsToggleRow(
                            title = "Publish Listening Status Feed",
                            subtitle = "Allow other users to see your active song loop",
                            checked = showActivityOn,
                            onCheckedChange = { viewModel.showActivityStatus.value = it }
                        )

                        Divider(color = Color.White.copy(alpha = 0.06f))

                        // Block management list
                        Column {
                            Text("Blocked Users Vault", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Block users from entering your private rooms", color = TextSubdued, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            // List of blocked users
                            if (blockedUsersList.isEmpty()) {
                                Text("No loopers blocked inside vault.", color = TextSubdued, fontSize = 11.sp, modifier = Modifier.padding(vertical = 4.dp))
                            } else {
                                blockedUsersList.forEach { blockedUser ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "❌ $blockedUser", color = Color.LightGray, fontSize = 12.sp)
                                        Text(
                                            text = "Unblock",
                                            color = NeonBlue,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable {
                                                viewModel.unblockUser(blockedUser)
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = customBlockInput,
                                    onValueChange = { customBlockInput = it },
                                    placeholder = { Text("Search/Type username...", fontSize = 11.sp) },
                                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonBlue
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (customBlockInput.isNotBlank()) {
                                            viewModel.blockUser(customBlockInput)
                                            customBlockInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Add Block", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // --- SECTION: ONBOARDING & GUIDES REPLAY --
            item {
                SettingsSectionHeader(title = "Product Onboarding & Guides")
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.navigateTo("onboarding")
                                }
                        ) {
                            Icon(Icons.Default.HelpCenter, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Replay Startup Onboarding Guide", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("View the swipable 5-slide core vision", color = TextSubdued, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // Version info banner
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("LoopTogether Setup v1.4.0", fontSize = 11.sp, color = TextSubdued)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Certified Secure OAuth Validation node active", fontSize = 10.sp, color = TextSubdued.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(top = 10.dp)
    )
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextSubdued, fontSize = 10.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonBlue,
                checkedTrackColor = NeonBlue.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun AccentCircle(
    colorName: String,
    brush: Brush,
    current: String,
    onClick: () -> Unit
) {
    val isSelected = current == colorName
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(brush)
            .border(3.dp, if (isSelected) Color.White else Color.Transparent, CircleShape)
            .clickable { onClick() }
    )
}
