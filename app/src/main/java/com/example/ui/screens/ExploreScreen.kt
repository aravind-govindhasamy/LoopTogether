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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlowCard
import com.example.ui.theme.*
import com.example.viewmodel.LoopTogetherViewModel
import coil.compose.AsyncImage

import com.example.ui.components.frostedGlassBackground

@Composable
fun ExploreScreen(viewModel: LoopTogetherViewModel) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val activeRoom by viewModel.activeRoom.collectAsState()

    var textInput by remember { mutableStateOf(searchQuery) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .frostedGlassBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 80.dp)
        ) {
            // Header
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text(
                    text = "Search & Discover",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Query YouTube songs, videos, and dynamic public hubs",
                    color = TextSubdued,
                    fontSize = 12.sp
                )
            }

            // Search Bar
            OutlinedTextField(
                value = textInput,
                onValueChange = {
                    textInput = it
                    viewModel.triggerSearch(it)
                },
                placeholder = { Text("Search songs, artists or genres...", color = TextSubdued, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonPurple) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = NeonBlue,
                    unfocusedBorderColor = DarkBorderNeon
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Predictive Search recommendation chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Synthwave", "Lofi Sunset", "Acoustic", "Chill").forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .clickable {
                                textInput = tag
                                viewModel.triggerSearch(tag)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "# $tag",
                            color = if (textInput == tag) NeonBlue else NeonPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Results stream
            val friends by viewModel.friends.collectAsState()
            val rooms by viewModel.availableRooms.collectAsState()

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (searchResults.isEmpty() && searchQuery.isBlank()) {
                    // ==========================================
                    // REAL-TIME DISCOVERY SECTIONS (PHASE 13)
                    // ==========================================
                    
                    // 1. Trending Stations & Rooms
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Trending Stations 🔥", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(HotPink.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("HIGH ENERGY", fontSize = 8.sp, color = HotPink, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            listOf(
                                Triple("Neon Tokyo Highway 🏎️", "LOFI-88", "CosmicDJ • 24 loopers"),
                                Triple("Sunset Coastline Retrowave 🌅", "SYNTH-88", "RetroSonic 🕶️ • 15 loopers")
                            ).forEach { trd ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            viewModel.joinRoomByCode(
                                                code = trd.second,
                                                onSuccess = { Toast.makeText(context, "Welcome to ${trd.first}!", Toast.LENGTH_SHORT).show() },
                                                onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                            )
                                        }
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp)),
                                    colors = CardDefaults.cardColors(containerColor = DarkSpaceSurface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(NeonPurple.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🔥", fontSize = 24.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(trd.first, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(trd.third, fontSize = 11.sp, color = TextSubdued)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(NeonPurple.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("TUNE IN", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Late-Night Sessions & Cozy Communities
                    item {
                        Column {
                            Text("Late-Night Cozy Nodes 🌙", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(10.dp))

                            listOf(
                                Triple("Subliminal Ambient Rain ☔", "VIBE-99", "Sarah_Sunset • 11 listening"),
                                Triple("Cozy Lofi Kitchen ☕", "LOFI-88", "LofiPanda 🐼 • 8 listening")
                            ).forEach { cozy ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            viewModel.joinRoomByCode(
                                                code = cozy.second,
                                                onSuccess = { Toast.makeText(context, "Welcome to ${cozy.first}!", Toast.LENGTH_SHORT).show() },
                                                onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                            )
                                        }
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp)),
                                    colors = CardDefaults.cardColors(containerColor = DarkSpaceSurface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(NeonBlue.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🌌", fontSize = 24.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(cozy.first, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(cozy.third, fontSize = 11.sp, color = TextSubdued)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(NeonBlue.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("FLOW", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Friends online in Orbits
                    item {
                        Column {
                            Text("Active in Your Music Circle 📡", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            val activeFriends = friends.filter { it.isOnline && it.activeRoomId != null }
                            if (activeFriends.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.03f))
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Circle is dormant. Connect with more friends to sync orbits!", fontSize = 11.sp, color = TextSubdued)
                                }
                            } else {
                                activeFriends.forEach { frnd ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                viewModel.joinRoomByCode(
                                                    code = frnd.activeRoomId!!,
                                                    onSuccess = { Toast.makeText(context, "Joined ${frnd.username}'s stream!", Toast.LENGTH_SHORT).show() },
                                                    onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                                )
                                            }
                                            .border(1.dp, HotPink.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                                        colors = CardDefaults.cardColors(containerColor = DarkSpaceSurface)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.08f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(frnd.profilePicUrl, fontSize = 20.sp)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(frnd.username, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("Vibing in: ${frnd.activeRoomName ?: "Private Lounge"}", fontSize = 11.sp, color = NeonBlue)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(HotPink.copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text("QUICK JOIN", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Recommended for You
                    item {
                        Column {
                            Text("Recommended for Your Taste ✨", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, NeonPurple.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                                colors = CardDefaults.cardColors(containerColor = NeonPurple.copy(alpha = 0.03f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("98% Synthwave Match Room ⚡", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Based on your interest in Kavinsky, Scandroid, and HOME. This communal station matches your late-night auditory signature perfectly.", fontSize = 11.sp, color = TextSubdued, lineHeight = 15.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            viewModel.joinRoomByCode(
                                                code = "SYNTH-88",
                                                onSuccess = {},
                                                onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                        modifier = Modifier.align(Alignment.End),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text("Sync Audio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                } else {
                    // ==========================================
                    // SEARCH RESULTS MODE (EXISTING BEHAVIOR)
                    // ==========================================
                    
                    // Warning if no room is entered
                    if (activeRoom == null) {
                        item {
                            GlowCard(
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = HotPink,
                                hasGlow = false
                            ) {
                                Text(
                                    text = "💡 Standby Mode Enabled",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HotPink
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "You are currently outside a synchronized listening party. Join or Host a room first from the Home panel to append tracks to a shared interactive queue!",
                                    fontSize = 11.sp,
                                    color = TextSubdued,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    } else {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Synced Queue Gateway: '${activeRoom?.name}'",
                                    color = NeonBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    items(searchResults) { song ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSpaceSurface)
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Video Thumbnail (Real Image)
                                if (song.coverUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = song.coverUrl,
                                        contentDescription = "Cover preview",
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.05f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = NeonPurple)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                }

                                // Info block
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 2,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = song.artist, // Channel Name
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = NeonBlue,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${viewModel.formatDuration(song.durationMs)}${if (song.viewCount.isNotEmpty()) " • " + song.viewCount else ""}",
                                        fontSize = 10.sp,
                                        color = TextSubdued
                                    )
                                    if (song.publishDate.isNotEmpty()) {
                                        Text(
                                            text = "Published: ${song.publishDate}",
                                            fontSize = 9.sp,
                                            color = TextSubdued.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Action buttons Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Play Now Button
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(NeonPurple, NeonBlue)))
                                            .clickable {
                                                viewModel.playSongNow(song)
                                                Toast.makeText(context, "Playing '${song.title}' now!", Toast.LENGTH_SHORT).show()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play Now",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Add to Queue button
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .clickable {
                                                if (activeRoom != null) {
                                                    viewModel.addSongToRoomQueue(song)
                                                    Toast.makeText(context, "Added '${song.title}' to queue!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    // Start Standalone / Solo Lounge directly so user is never blocked!
                                                    viewModel.playSongNow(song)
                                                    Toast.makeText(context, "Starting standalone play!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Append Queue",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
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
