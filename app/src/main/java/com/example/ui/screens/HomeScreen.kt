package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RoomEntity
import com.example.data.SongSearchModel
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.LoopTogetherViewModel
import kotlinx.coroutines.launch

import com.example.ui.components.frostedGlassBackground

@Composable
fun HomeScreen(viewModel: LoopTogetherViewModel) {
    val loopColors = com.example.ui.theme.LocalLoopColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val user by viewModel.currentUser.collectAsState()
    val rooms by viewModel.availableRooms.collectAsState()
    
    // UI state
    var inviteCodeInput by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Create room inputs
    var newRoomName by remember { mutableStateOf("") }
    var newRoomDesc by remember { mutableStateOf("") }
    var isPublicRoom by remember { mutableStateOf(true) }

    // AI recommendation block
    var aiRecPrompt by remember { mutableStateOf("Suggest high-energy synthpop tracks for a weekend party") }
    var loadedAiRecs by remember { mutableStateOf<String?>(null) }
    var isGeneratingAi by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .frostedGlassBackground()
    ) {
        // Main Scrollable Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 80.dp) // Cushion for bottom navigation bar
        ) {
            // Static Top Header Block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, ${user?.username ?: "Explorer"}!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = loopColors.textPrimary
                    )
                    Text(
                        text = "System active • ${rooms.size} rooms online",
                        fontSize = 12.sp,
                        color = loopColors.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Profile Avatar Hub
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(loopColors.secondary.copy(alpha = 0.2f))
                        .border(1.dp, loopColors.secondary, CircleShape)
                        .clickable { viewModel.navigateTo("profile") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = user?.profilePicUrl ?: "🎧", fontSize = 24.sp)
                }
            } // Close the static header Row

            // User Selected Tab Indicator
            var selectedTab by remember { mutableStateOf(0) } // 0: Live Frequencies, 1: Music Circle, 2: Milestones & Achievements
            
            val friends by viewModel.friends.collectAsState()
            val friendRequests by viewModel.friendRequests.collectAsState()
            val scheduledEvents by viewModel.scheduledEvents.collectAsState()
            val memoryMoments by viewModel.memoryMoments.collectAsState()
            val achievements by viewModel.achievements.collectAsState()
            val activeRoomCtx by viewModel.activeRoom.collectAsState()

            var newFriendInput by remember { mutableStateOf("") }
            var editingFriendId by remember { mutableStateOf<String?>(null) }
            var friendNoteText by remember { mutableStateOf("") }

            // Segmented Top Navigation Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("📻 Frequencies", "👥 Music Circle", "🏆 Milestones").forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedTab == index) loopColors.primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                            .border(1.dp, if (selectedTab == index) loopColors.primary else Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (selectedTab == index) loopColors.textPrimary else loopColors.textSubdued,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (selectedTab == 0) {
                    // ==========================================
                    // TAB 0: FREQUENCIES (LIVE HUB & DISCOVERY)
                    // ==========================================

                    // 1. Quick Join Code input box
                    item {
                        GlowCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = loopColors.primary,
                            hasGlow = false
                        ) {
                            Text(
                                text = "Connect Synchronized Room",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = loopColors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Enter invite key (e.g., SYNTH-88)",
                                fontSize = 11.sp,
                                color = loopColors.textSubdued
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = inviteCodeInput,
                                    onValueChange = { inviteCodeInput = it.uppercase() },
                                    placeholder = { Text("Code e.g. LOFI-77", color = loopColors.textSubdued, fontSize = 13.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = loopColors.textPrimary,
                                        unfocusedTextColor = loopColors.textPrimary,
                                        focusedBorderColor = loopColors.primary,
                                        unfocusedBorderColor = loopColors.border
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Button(
                                    onClick = {
                                        if (inviteCodeInput.isBlank()) {
                                            Toast.makeText(context, "Please write a room code!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.joinRoomByCode(
                                                code = inviteCodeInput,
                                                onSuccess = {
                                                    Toast.makeText(context, "Succeeded connecting synchronized stream!", Toast.LENGTH_SHORT).show()
                                                },
                                                onError = { error ->
                                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = loopColors.primary),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = if (loopColors.isDark) Color(0xFF020205) else Color.White
                                    )
                                }
                            }
                        }
                    }

                    // 2. Action Grid (Create, Explore)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Create Room Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(95.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Brush.linearGradient(listOf(NeonPurple, HotPink)))
                                    .clickable { showCreateDialog = true }
                                    .padding(14.dp)
                            ) {
                                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Create Room", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Max 10 loopers", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                }
                            }

                            // Explore Rooms Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(95.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DarkSpaceSurface)
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                    .clickable { viewModel.navigateTo("explore") }
                                    .padding(14.dp)
                            ) {
                                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                                    Icon(Icons.Default.Language, contentDescription = null, tint = NeonBlue)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Public Stations", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Discover live play", fontSize = 10.sp, color = TextSubdued)
                                }
                            }
                        }
                    }

                    // COMMUNITY GENRE LOUNGES
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Genre Communities & Lounges",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeonPurple.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("LIVE CHANNELS", fontSize = 8.sp, color = NeonPurple, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(listOf(
                                    Triple("Synthwave Outrun ⚡", "LOFI-88", "12 loopers vibing"),
                                    Triple("Cozy Sunset Lofi 🌆", "SYNTH-88", "18 loopers active"),
                                    Triple("Cyberpunk Ambient 🤖", "VIBE-99", "6 loopers connected")
                                )) { lounge ->
                                    Card(
                                        modifier = Modifier
                                            .width(180.dp)
                                            .clickable {
                                                viewModel.joinRoomByCode(
                                                    code = lounge.second,
                                                    onSuccess = {},
                                                    onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                                )
                                            }
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                                        colors = CardDefaults.cardColors(containerColor = DarkSpaceSurface)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(lounge.first, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(lounge.third, fontSize = 10.sp, color = TextSubdued)
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Join Lounge", fontSize = 9.sp, color = NeonPurple, fontWeight = FontWeight.Bold)
                                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Active Rooms list
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Live Tunnels Feed",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = HotPink,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (rooms.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = TranslucentGlassCard),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                EmptyStateView(
                                    icon = Icons.Default.MusicNote,
                                    title = "No Active Listening Broadcasts",
                                    description = "There are no live active listening tunnels right now. Connect your frequencies or spark your own streaming party!",
                                    actionText = "Host Custom Party",
                                    color = NeonPurple,
                                    onActionClick = {
                                        viewModel.hostNewRoom("Midnight Vibing Tunnel", "Cozy custom tunnel space", true)
                                    }
                                )
                            }
                        }
                    } else {
                        items(rooms) { room ->
                            GlowCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.joinRoomByCode(
                                            code = room.id,
                                            onSuccess = {},
                                            onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                        )
                                    },
                                borderColor = if (room.isPlaying) NeonPurple else Color.Gray,
                                hasGlow = room.isPlaying
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(if (room.isPlaying) ActiveGreen else Color.Gray)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = room.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = room.description,
                                            fontSize = 12.sp,
                                            color = TextSubdued,
                                            maxLines = 1
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${room.currentSongTitle} • ${room.currentSongArtist}",
                                                fontSize = 11.sp,
                                                color = NeonBlue,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(horizontalAlignment = Alignment.End) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(NeonBlue.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${room.memberCount}/10 online",
                                                fontSize = 10.sp,
                                                color = NeonBlue,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "By ${room.hostUsername}",
                                            fontSize = 10.sp,
                                            color = TextSubdued
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // SCHEDULED SESSIONS & ALBUM PARTIES
                    item {
                        Column {
                            Text(
                                text = "Scheduled Events & Album Release Parties",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            scheduledEvents.forEach { ev ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp)),
                                    colors = CardDefaults.cardColors(containerColor = if (ev.userRsvped) NeonPurple.copy(alpha = 0.05f) else DarkSpaceSurface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(HotPink.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                            ) {
                                                Text(ev.genre, color = HotPink, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Text(
                                                text = ev.countdownText,
                                                color = NeonBlue,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = ev.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = ev.description, fontSize = 11.sp, color = TextSubdued, lineHeight = 14.sp)
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Hosted by ${ev.hostUsername} • ${ev.rsvpsCount} RSVPs", fontSize = 10.sp, color = TextSubdued)
                                            Button(
                                                onClick = { viewModel.toggleEventRsvp(ev.id) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (ev.userRsvped) NeonPurple else Color.White.copy(alpha = 0.1f)
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text(if (ev.userRsvped) "RSVP'd ✓" else "RSVP Now", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Gemini AI Music Recommendations
                    item {
                        GlowCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = HotPink,
                            hasGlow = true
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = HotPink)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LoopDJ Gemini Assistant",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Get real-time recommendation nodes for synchronized parties.",
                                fontSize = 12.sp,
                                color = TextSubdued
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = aiRecPrompt,
                                onValueChange = { aiRecPrompt = it },
                                label = { Text("What is the room mood?", fontSize = 11.sp) },
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = HotPink,
                                    unfocusedBorderColor = DarkBorderNeon
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (isGeneratingAi) {
                                CircularProgressIndicator(color = HotPink, modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
                            } else {
                                Button(
                                    onClick = {
                                        isGeneratingAi = true
                                        scope.launch {
                                            loadedAiRecs = com.example.data.GeminiClient.generateAiContent(
                                                prompt = "Provide 3 recommended songs for the mood: $aiRecPrompt. Keep it brief.",
                                                systemPrompt = "You are a professional music consultant bot."
                                            )
                                            isGeneratingAi = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HotPink),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Ask Gemini DJ ✨", fontWeight = FontWeight.Bold)
                                }
                            }

                            loadedAiRecs?.let { response ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = HotPink.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = response,
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                } else if (selectedTab == 1) {
                    // ==========================================
                    // TAB 1: MUSIC CIRCLE (FRIENDS SYSTEM)
                    // ==========================================

                    // 1. Pending Friend Requests Notification Card
                    if (friendRequests.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, NeonBlue, RoundedCornerShape(14.dp)),
                                colors = CardDefaults.cardColors(containerColor = NeonBlue.copy(alpha = 0.08f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Pending Connections", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    friendRequests.forEach { req ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White.copy(alpha = 0.08f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(req.profilePicUrl, fontSize = 14.sp)
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(req.username, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Button(
                                                    onClick = { viewModel.respondToFriendRequest(req.id, true) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(28.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Accept", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Button(
                                                    onClick = { viewModel.respondToFriendRequest(req.id, false) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.06f)),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(28.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Ignore", fontSize = 10.sp, color = TextSubdued)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Add New Friend box
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkSpaceSurface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Expand Friendship Network", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Add a friend's musical handle below to join orbits.", fontSize = 11.sp, color = TextSubdued)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = newFriendInput,
                                        onValueChange = { newFriendInput = it },
                                        placeholder = { Text("Enter tag e.g. SoundWave", color = TextSubdued, fontSize = 12.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White, focusedBorderColor = NeonPurple
                                        )
                                    )
                                    Button(
                                        onClick = {
                                            if (newFriendInput.isNotBlank()) {
                                                viewModel.addFriend(newFriendInput)
                                                newFriendInput = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                                    ) {
                                        Text("Invite")
                                    }
                                }
                            }
                        }
                    }

                    // 3. Simulated Live Friend Activity Cards (Alex listening to, Sarah started, etc.)
                    item {
                        Column {
                            Text("Realtime Activity Stream", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            listOf(
                                Triple("RetroSonic 🕶️", "Listening to Outrun Synths", "HOME - Resonance 🌅"),
                                Triple("Sarah_Sunset 🌸", "Created Warm Lofi Cafe", "We're Finally Landing ☕"),
                                Triple("WaveRider 🌌", "Active inside Space Lounge", "Cyberpunk Atmosphere 🤖")
                            ).forEach { act ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp)),
                                    colors = CardDefaults.cardColors(containerColor = DarkSpaceSurface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(NeonPurple.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🎵", fontSize = 16.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${act.first} is active now",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${act.second} • ${act.third}",
                                                color = TextSubdued,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(NeonBlue.copy(alpha = 0.12f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("Presence Live", fontSize = 8.sp, color = NeonBlue, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Friends List Directory with status badges, Close Friends, Notes & Room invitation triggers
                    item {
                        Text("Auditory Connection directory (${friends.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    items(friends) { friend ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (friend.isCloseFriend) HotPink.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f),
                                    RoundedCornerShape(14.dp)
                                ),
                            colors = CardDefaults.cardColors(containerColor = DarkSpaceSurface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Status Presence indicators
                                    Box(modifier = Modifier.size(42.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.08f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(friend.profilePicUrl, fontSize = 20.sp)
                                        }
                                        // Colored presence spot
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (friend.statusType) {
                                                        "Online" -> ActiveGreen
                                                        "Idle" -> Color.Yellow
                                                        "Away" -> HotPink
                                                        else -> Color.Gray
                                                    }
                                                )
                                                .border(1.5.dp, Color(0xFF020205), CircleShape)
                                                .align(Alignment.BottomEnd)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(friend.username, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            if (friend.isCloseFriend) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(HotPink.copy(alpha = 0.15f))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text("CLOSE", color = HotPink, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            if (friend.isFavorite) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Default.Star, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (friend.currentlyListening.isNotBlank()) "Listening: ${friend.currentlyListening}" else friend.statusText,
                                            fontSize = 11.sp,
                                            color = if (friend.currentlyListening.isNotBlank()) NeonBlue else TextSubdued,
                                            maxLines = 1
                                        )
                                    }

                                    // Match indicator
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${friend.compatibility}%", fontSize = 12.sp, fontWeight = FontWeight.Black, color = NeonBlue)
                                        Text("Match", fontSize = 8.sp, color = TextSubdued)
                                    }
                                }

                                // Interactive Quick Buttons (Notes, Favourite Close, Join Room if active, One-Tap invite)
                                Spacer(modifier = Modifier.height(10.dp))
                                Divider(color = Color.White.copy(alpha = 0.06f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        // Notes toggle / editor trigger
                                        IconButton(
                                            onClick = {
                                                if (editingFriendId == friend.id) {
                                                    editingFriendId = null
                                                } else {
                                                    editingFriendId = friend.id
                                                    friendNoteText = friend.notes
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.EditNote,
                                                contentDescription = "Notes",
                                                tint = if (editingFriendId == friend.id) NeonPurple else TextSubdued,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Close friend toggle
                                        IconButton(
                                            onClick = { viewModel.toggleCloseFriend(friend.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = "Close Friend",
                                                tint = if (friend.isCloseFriend) HotPink else TextSubdued,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Favorite toggle
                                        IconButton(
                                            onClick = { viewModel.toggleFriendFavorite(friend.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Favorite",
                                                tint = if (friend.isFavorite) Color.Yellow else TextSubdued,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        // One-Tap Invite Dispatch if in a room
                                        if (activeRoomCtx != null) {
                                            Button(
                                                onClick = { viewModel.sendQuickRoomInvite(friend.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue.copy(alpha = 0.15f)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(26.dp),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("Invite ✉️", fontSize = 9.sp, color = NeonBlue, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Quick join friend's room if listening
                                        if (friend.activeRoomId != null) {
                                            Button(
                                                onClick = {
                                                    viewModel.joinRoomByCode(
                                                        code = friend.activeRoomId,
                                                        onSuccess = { Toast.makeText(context, "Joined ${friend.username}'s stream!", Toast.LENGTH_SHORT).show() },
                                                        onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(26.dp),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("Quick Join", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                // Note Inline Editor Drawer
                                if (editingFriendId == friend.id) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = friendNoteText,
                                        onValueChange = { friendNoteText = it },
                                        placeholder = { Text("Write local note about ${friend.username}...", fontSize = 11.sp, color = TextSubdued) },
                                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = NeonPurple)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = {
                                            viewModel.updateFriendNote(friend.id, friendNoteText)
                                            editingFriendId = null
                                            Toast.makeText(context, "Friend note cached!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                        modifier = Modifier.align(Alignment.End),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Save Note", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Show note preview if present and not editing
                                if (friend.notes.isNotBlank() && editingFriendId != friend.id) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.03f))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "Note: ${friend.notes}",
                                            fontSize = 10.sp,
                                            color = TextSubdued,
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                } else {
                    // ==========================================
                    // TAB 2: MILESTONES (ACHIEVEMENTS / MEMORIES)
                    // ==========================================

                    // 1. Social Memories list
                    item {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Celebration, contentDescription = null, tint = NeonPurple)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Auditory Social Memories", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Your historic shared sessions inside synchronized tunnels.", fontSize = 11.sp, color = TextSubdued)
                            Spacer(modifier = Modifier.height(12.dp))

                            memoryMoments.forEach { moment ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp)),
                                    colors = CardDefaults.cardColors(containerColor = DarkSpaceSurface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(moment.icon, fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column {
                                            Text(moment.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(moment.description, fontSize = 11.sp, color = TextSubdued, lineHeight = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Achievements Suite
                    item {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = HotPink)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Minimalist Milestones", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Subtle milestones demonstrating your listening progress.", fontSize = 11.sp, color = TextSubdued)
                            Spacer(modifier = Modifier.height(12.dp))

                            achievements.forEach { block ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .border(
                                            1.dp,
                                            if (block.unlocked) NeonPurple.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.06f),
                                            RoundedCornerShape(14.dp)
                                        ),
                                    colors = CardDefaults.cardColors(containerColor = if (block.unlocked) NeonPurple.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(if (block.unlocked) NeonPurple.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(block.icon, fontSize = 20.sp)
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = block.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (block.unlocked) Color.White else TextSubdued
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(block.description, fontSize = 11.sp, color = TextSubdued)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (block.unlocked) ActiveGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.06f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (block.unlocked) "COMPLETED" else "LOCKED",
                                                color = if (block.unlocked) ActiveGreen else TextSubdued,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } // Close LazyColumn
        } // Close layout Column

        // Animated Dialog: Create Room Screen configuration
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                containerColor = Color(0xFF09090E),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Audiotrack, contentDescription = null, tint = NeonPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Establish Stereo Room", color = Color.White)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = newRoomName,
                            onValueChange = { newRoomName = it },
                            label = { Text("Room Title") },
                            placeholder = { Text("e.g. Synth Sunset Outrun") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = NeonPurple)
                        )

                        OutlinedTextField(
                            value = newRoomDesc,
                            onValueChange = { newRoomDesc = it },
                            label = { Text("Slogan / Vibe description") },
                            placeholder = { Text("Chill drive beats with dynamic glow") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = NeonPurple)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Public Access Point", color = Color.White, modifier = Modifier.weight(1f))
                            Switch(
                                checked = isPublicRoom,
                                onCheckedChange = { isPublicRoom = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonBlue)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showCreateDialog = false
                            viewModel.hostNewRoom(
                                roomName = newRoomName,
                                description = newRoomDesc,
                                isPublic = isPublicRoom
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                    ) {
                        Text("Create and Launch")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Back", color = TextSubdued)
                    }
                }
            )
        }
    }
}
