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
    val loopColors = com.example.ui.theme.LocalLoopColors.current

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
    val currentTheme by viewModel.appTheme.collectAsState()
    val motionIntensity by viewModel.motionIntensity.collectAsState()
    val blurIntensity by viewModel.blurIntensity.collectAsState()
    val ambientEffectsToggle by viewModel.ambientEffectsToggle.collectAsState()
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

            // --- SECTION: REALTIME SYNC SERVER ---
            item {
                SettingsSectionHeader(title = "Realtime Sync Server")
            }

            item {
                val socketService = viewModel.socketService
                val connState by viewModel.socketConnectionState.collectAsState()
                var serverHostInput by remember { mutableStateOf(socketService.getServerHostOnly()) }

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
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Socket Stream Status",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            val statusLabel: String
                            val statusColor: Color
                            when (connState) {
                                com.example.data.ConnectionState.CONNECTED -> {
                                    statusLabel = "CONNECTED ●"
                                    statusColor = NeonBlue
                                }
                                com.example.data.ConnectionState.CONNECTING -> {
                                    statusLabel = "CONNECTING..."
                                    statusColor = NeonPurple
                                }
                                com.example.data.ConnectionState.LOCAL_SYNC -> {
                                    statusLabel = "LOCAL IDEMPOTENT ●"
                                    statusColor = HotPink
                                }
                                else -> {
                                    statusLabel = "OFFLINE"
                                    statusColor = Color.Red
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(statusColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = statusLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = statusColor
                                )
                            }
                        }

                        Text(
                            text = "To sync playlists, search live YouTube, and stream audio together with peers, connect to your workspace server node.",
                            color = TextSubdued,
                            fontSize = 11.sp
                        )

                        OutlinedTextField(
                            value = serverHostInput,
                            onValueChange = { serverHostInput = it },
                            placeholder = { Text("e.g. 10.0.2.2:3000 or ais-dev-*.run.app", color = TextSubdued, fontSize = 12.sp) },
                            singleLine = true,
                            label = { Text("Server Host Address", color = NeonPurple, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonBlue,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    socketService.updateServerAddress(serverHostInput)
                                    Toast.makeText(context, "Re-connecting to $serverHostInput...", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    serverHostInput = "10.0.2.2:3000"
                                    socketService.updateServerAddress("10.0.2.2:3000")
                                    Toast.makeText(context, "Reset to standard local emulator port", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Reset Local", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
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
                SettingsSectionHeader(title = "Handcrafted Visual Identity")
            }

            item {
                Text(
                    text = "Select Premium Themes",
                    color = loopColors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(com.example.ui.theme.LoopTheme.values()) { themeOption ->
                        val isSelected = currentTheme == themeOption
                        val themeColorSample = when(themeOption) {
                            com.example.ui.theme.LoopTheme.MIDNIGHT_PULSE -> com.example.ui.theme.MidnightPulseColors
                            com.example.ui.theme.LoopTheme.SUNSET_VIBES -> com.example.ui.theme.SunsetVibesColors
                            com.example.ui.theme.LoopTheme.AURORA -> com.example.ui.theme.AuroraColors
                            com.example.ui.theme.LoopTheme.VELVET_DARK -> com.example.ui.theme.VelvetDarkColors
                            com.example.ui.theme.LoopTheme.PURE_LIGHT -> com.example.ui.theme.PureLightColors
                            com.example.ui.theme.LoopTheme.DYNAMIC_REACTIVE -> com.example.ui.theme.MidnightPulseColors.copy(primary = Color(0xFFEC4899), secondary = Color(0xFF22D3EE))
                        }

                        Card(
                            modifier = Modifier
                                .width(135.dp)
                                .height(85.dp)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    brush = if (isSelected) {
                                        Brush.linearGradient(listOf(themeColorSample.primary, themeColorSample.secondary))
                                    } else {
                                        Brush.linearGradient(listOf(loopColors.border, loopColors.border))
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    viewModel.appTheme.value = themeOption
                                    viewModel.cosmicMidnightTheme.value = (themeOption != com.example.ui.theme.LoopTheme.PURE_LIGHT)
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (themeOption == com.example.ui.theme.LoopTheme.PURE_LIGHT) Color(0xFFF1F5F9) else themeColorSample.background.copy(alpha = 0.95f)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Row(
                                    modifier = Modifier.align(Alignment.TopEnd),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(themeColorSample.primary))
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(themeColorSample.secondary))
                                }

                                Text(
                                    text = themeOption.displayName,
                                    color = if (themeOption == com.example.ui.theme.LoopTheme.PURE_LIGHT) Color(0xFF1E293B) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, loopColors.border, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = loopColors.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Personalize accents
                        Column {
                            Text("Theme Color Accent", color = loopColors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Personalizes specific functional indicators", color = loopColors.textSubdued, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val accents = listOf(
                                    "purple" to Color(0xFFA855F7),
                                    "blue" to Color(0xFF3B82F6),
                                    "pink" to Color(0xFFEC4899),
                                    "cyan" to Color(0xFF22D3EE),
                                    "emerald" to Color(0xFF10B981),
                                    "rose" to Color(0xFFFDA4AF),
                                    "orange" to Color(0xFFF97316),
                                    "indigo" to Color(0xFF4F46E5)
                                )
                                accents.forEach { accent ->
                                    val isPicked = accentColorIndex == accent.first
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(accent.second)
                                            .border(
                                                2.5.dp,
                                                if (isPicked) loopColors.textPrimary else Color.Transparent,
                                                CircleShape
                                            )
                                            .clickable {
                                                viewModel.accentColorIndex.value = accent.first
                                            }
                                    )
                                }
                            }
                        }

                        Divider(color = loopColors.border.copy(alpha = 0.5f))

                        // Ambient effects toggle row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Aura Particles & Ambient Effects", color = loopColors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Enables floating star particles and reactive pulse orbits", color = loopColors.textSubdued, fontSize = 10.sp)
                            }
                            Switch(
                                checked = ambientEffectsToggle,
                                onCheckedChange = { viewModel.ambientEffectsToggle.value = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = loopColors.primary,
                                    checkedTrackColor = loopColors.primary.copy(alpha = 0.3f)
                                )
                            )
                        }

                        Divider(color = loopColors.border.copy(alpha = 0.5f))

                        // Motion Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Motion Speed Intensity", color = loopColors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(String.format("%.1fx", motionIntensity), color = loopColors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Speeds up background drift, canvas neons and equalizers", color = loopColors.textSubdued, fontSize = 10.sp)
                            Slider(
                                value = motionIntensity,
                                onValueChange = { viewModel.motionIntensity.value = it },
                                valueRange = 0f..2f,
                                colors = SliderDefaults.colors(
                                    thumbColor = loopColors.primary,
                                    activeTrackColor = loopColors.primary.copy(alpha = 0.4f),
                                    inactiveTrackColor = loopColors.textSubdued.copy(alpha = 0.2f)
                                )
                            )
                        }

                        Divider(color = loopColors.border.copy(alpha = 0.5f))

                        // Blur Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Glass Blur Intensity", color = loopColors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${blurIntensity.toInt()} px", color = loopColors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Deepness blur spacing of glassy background panels", color = loopColors.textSubdued, fontSize = 10.sp)
                            Slider(
                                value = blurIntensity,
                                onValueChange = { viewModel.blurIntensity.value = it },
                                valueRange = 0f..32f,
                                colors = SliderDefaults.colors(
                                    thumbColor = loopColors.primary,
                                    activeTrackColor = loopColors.primary.copy(alpha = 0.4f),
                                    inactiveTrackColor = loopColors.textSubdued.copy(alpha = 0.2f)
                                )
                            )
                        }

                        Divider(color = loopColors.border.copy(alpha = 0.5f))

                        // Reduced Motion Mode Toggle
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Reduced Motion Mode", color = loopColors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Disables rotating canvases to prioritize battery life", color = loopColors.textSubdued, fontSize = 10.sp)
                            }
                            Switch(
                                checked = reducedMotionOn,
                                onCheckedChange = { viewModel.reducedMotionEnabled.value = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = loopColors.primary,
                                    checkedTrackColor = loopColors.primary.copy(alpha = 0.3f)
                                )
                            )
                        }
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
