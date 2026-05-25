package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
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
fun SupportScreen(viewModel: LoopTogetherViewModel) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    
    // Bug / Ticket states
    var bugSubject by remember { mutableStateOf("") }
    var bugDescription by remember { mutableStateOf("") }
    var selectedDevice by remember { mutableStateOf("Android Phone") }
    
    val deviceTypeList = listOf("Android Phone", "Tablet / Foldable", "DeX Desktop", "Rugged Handheld")

    // FAQ Items databank
    val faqs = listOf(
        FaqItem(
            q = "How do I join a listening room?",
            a = "Under the Home Hub screen, enter an active 6-letter room code (e.g., ROCK-90) into the join ticket portal. Alternatively, find public active tunnels in the Search & Explore feed and tap Join."
        ),
        FaqItem(
            q = "Why is my audio out of sync with other loopers?",
            a = "Millisecond offset delays are common under high-jitter networks. Open App Settings, and increase or decrease your 'Sync Calibration' latency offset slider by appropriate values to realign with the host's play clock."
        ),
        FaqItem(
            q = "Can I stream playlists from Spotify or Apple Music directly?",
            a = "Currently loop pools support billions of track URLs from public YouTube APIs to guarantee high-throughput, cross-platform video sync. Direct integration for private audio caches is underway."
        ),
        FaqItem(
            q = "What is wired hardware output optimization?",
            a = "Enabling wired hardware output overrides wireless Bluetooth buffering and formats audio queues specifically for lowest latency. Unwired/Bluetooth headsets may incur standard headphone-grade hardware latency."
        ),
        FaqItem(
            q = "How do I host a private room for a couple or study session?",
            a = "Tap the float action '+' button on the Home Hub, type your tunnel title, and toggle the 'Make public' switch off. You will receive a unique invitation code to share via private chats."
        ),
        FaqItem(
            q = "What happens if the host disconnects?",
            a = "LoopTogether automatically designates the next active looper in the member queue as host, preserving queue items, synchronizations, and conversation logs cleanly."
        )
    )

    val filteredFaqs = remember(searchQuery) {
        if (searchQuery.isBlank()) faqs
        else faqs.filter { it.q.contains(searchQuery, ignoreCase = true) || it.a.contains(searchQuery, ignoreCase = true) }
    }

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
                        text = "Vibe Support Center",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Search Bar topic
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search troubleshooting or FAQ topics...", fontSize = 12.sp, color = TextSubdued) },
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = Color.White),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSubdued) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        unfocusedContainerColor = DarkSpaceSurface,
                        focusedContainerColor = DarkSpaceSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // FAQ Title
            item {
                Text(
                    text = "Frequently Questioned Nodes",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Expanded Accordions
            if (filteredFaqs.isEmpty()) {
                item {
                    Text(
                        "No topics found for \"$searchQuery\". Try checking for calibration rules or sync issues.",
                        color = TextSubdued,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(filteredFaqs) { faq ->
                    var isExpanded by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                            .clickable { isExpanded = !isExpanded },
                        colors = CardDefaults.cardColors(containerColor = DarkSpaceSurface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = faq.q,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = NeonBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = faq.a,
                                        color = TextSubdued,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bug submission portal
            item {
                Text(
                    text = "Submit Vibe Diagnostic Report",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                GlowCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = HotPink,
                    hasGlow = false
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "FOUND A BUG IN TRANSMISSION?",
                            fontSize = 9.sp,
                            color = HotPink,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Text(
                            "Direct logs, sync delays and playback halts can be logged directly with our enterprise dev team.",
                            fontSize = 11.sp,
                            color = TextSubdued,
                            lineHeight = 15.sp
                        )

                        OutlinedTextField(
                            value = bugSubject,
                            onValueChange = { bugSubject = it },
                            label = { Text("Subject", fontSize = 11.sp) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, focusedBorderColor = HotPink
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = bugDescription,
                            onValueChange = { bugDescription = it },
                            label = { Text("Details (What happened)", fontSize = 11.sp) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, focusedBorderColor = HotPink
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Device Class", fontSize = 11.sp, color = TextSubdued, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            deviceTypeList.forEach { dev ->
                                val selected = selectedDevice == dev
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) HotPink.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f))
                                        .border(1.dp, if (selected) HotPink else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { selectedDevice = dev }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(dev, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (bugSubject.isBlank() || bugDescription.isBlank()) {
                                    Toast.makeText(context, "Fill in report parameters first!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.postActivityEvent("Diagnostic ticket submitted successfully! 🎟️")
                                    Toast.makeText(context, "Vibe ticket submitted to security nodes!", Toast.LENGTH_SHORT).show()
                                    bugSubject = ""
                                    bugDescription = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HotPink),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Dispatch Diagnostic Report", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

data class FaqItem(val q: String, val a: String)
