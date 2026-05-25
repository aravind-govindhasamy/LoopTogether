package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlowCard
import com.example.ui.components.frostedGlassBackground
import com.example.ui.theme.*
import com.example.viewmodel.LoopTogetherViewModel

@Composable
fun ContactScreen(viewModel: LoopTogetherViewModel) {
    val context = LocalContext.current
    
    var feedbackType by remember { mutableStateOf("Feature Proposal") }
    var feedbackText by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    
    val categoryList = listOf("Feature Proposal", "Partnership", "UI Refinement", "General Praise")

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
                        text = "Contact LoopTogether",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Quick Contact Info
            item {
                GlowCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NeonBlue,
                    hasGlow = true
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "LEAVE A TRANSMISSION SIGNAL",
                            fontSize = 9.sp,
                            color = NeonBlue,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "We are responsive humans, not generic algorithms.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "support@looptogether.io",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Average response expectation: <12 hours ⚡",
                            fontSize = 10.sp,
                            color = TextSubdued,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Social channels
            item {
                Text(
                    text = "Vibe Social Networks",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SocialCard(
                        icon = Icons.Default.Hub,
                        platformName = "GitHub Hub",
                        handle = "@looptogether",
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                    SocialCard(
                        icon = Icons.Default.Chat,
                        platformName = "Discord Pulse",
                        handle = "join.gg/soundwaves",
                        color = NeonBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Inquiries Feedback form
            item {
                Text(
                    text = "Startup Feedback & Proposal Node",
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = userEmail,
                            onValueChange = { userEmail = it },
                            label = { Text("Your Email (For responses)", fontSize = 11.sp) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, focusedBorderColor = NeonBlue
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Proposal Dimension", fontSize = 11.sp, color = TextSubdued, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categoryList.take(2).forEach { cat ->
                                val selected = feedbackType == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) NeonBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f))
                                        .border(1.dp, if (selected) NeonBlue else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { feedbackType = cat }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cat, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categoryList.drop(2).forEach { cat ->
                                val selected = feedbackType == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) NeonBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f))
                                        .border(1.dp, if (selected) NeonBlue else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { feedbackType = cat }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cat, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = feedbackText,
                            onValueChange = { feedbackText = it },
                            label = { Text("Write your proposal or review...", fontSize = 11.sp) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, focusedBorderColor = NeonBlue
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (feedbackText.isBlank()) {
                                    Toast.makeText(context, "Please write feedback text first!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.postActivityEvent("Proposal submitted! We will analyze this code flow.")
                                    Toast.makeText(context, "Feedback successfully sent to support center!", Toast.LENGTH_SHORT).show()
                                    feedbackText = ""
                                    userEmail = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Transmit Signal Proposal", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SocialCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    platformName: String,
    handle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSpaceSurface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(platformName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(handle, color = TextSubdued, fontSize = 9.sp)
        }
    }
}
