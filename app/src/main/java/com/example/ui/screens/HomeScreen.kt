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
import com.example.ui.components.GlowCard
import com.example.ui.components.GlassSurface
import com.example.ui.theme.*
import com.example.viewmodel.LoopTogetherViewModel
import kotlinx.coroutines.launch

import com.example.ui.components.frostedGlassBackground

@Composable
fun HomeScreen(viewModel: LoopTogetherViewModel) {
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
                        color = Color.White
                    )
                    Text(
                        text = "System active • ${rooms.size} rooms online",
                        fontSize = 12.sp,
                        color = NeonBlue,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Profile Avatar Hub
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(NeonPurple.copy(alpha = 0.2f))
                        .border(1.dp, NeonPurple, CircleShape)
                        .clickable { viewModel.navigateTo("profile") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = user?.profilePicUrl ?: "🎧", fontSize = 24.sp)
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Quick Join Code input box
                item {
                    GlowCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = NeonBlue,
                        hasGlow = false
                    ) {
                        Text(
                            text = "Connect Synchronized Room",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enter invite key (e.g., SYNTH-88)",
                            fontSize = 11.sp,
                            color = TextSubdued
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
                                placeholder = { Text("Code e.g. LOFI-77", color = TextSubdued, fontSize = 13.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = NeonBlue,
                                    unfocusedBorderColor = DarkBorderNeon
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
                                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = CosmicBackground)
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

                // 3. Active Rooms list
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Tunnels Feed",
                            fontSize = 17.sp,
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
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No Active Rooms", color = Color.White, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Launch your own custom listening party or checkout the public lists!", color = TextSubdued, fontSize = 11.sp)
                            }
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

                // 4. Friends list
                item {
                    Text(
                        text = "Vibing Friends (Simulated Hub)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(listOf(
                            Pair("RetroSonic", "Playing: Resonance 🌅"),
                            Pair("BeatMaker", "Playing: Chill Beats ☕"),
                            Pair("Looper99", "Offline • 2h ago"),
                            Pair("VibeMaster", "Playing: Blinding Lights 🔥")
                        )) { friend ->
                            Box(
                                modifier = Modifier
                                    .width(135.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSpaceSurface)
                                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.05f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🦊", fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(friend.first, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(friend.second, fontSize = 9.sp, color = TextSubdued, maxLines = 1)
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
            }
        }

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
