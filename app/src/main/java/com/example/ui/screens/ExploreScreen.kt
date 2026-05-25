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
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
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

                            // Action button
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (activeRoom != null) NeonPurple.copy(alpha = 0.2f)
                                        else Color.White.copy(alpha = 0.05f)
                                    )
                                    .clickable {
                                        if (activeRoom != null) {
                                            viewModel.addSongToRoomQueue(song)
                                            Toast
                                                .makeText(
                                                    context,
                                                    "Successfully added '${song.title}' to the collaborative queue!",
                                                    Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        } else {
                                            viewModel.navigateTo("home")
                                            Toast
                                                .makeText(
                                                    context,
                                                    "Host or join a listing room first!",
                                                    Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Append Queue",
                                    tint = if (activeRoom != null) NeonPurple else Color.Gray,
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
