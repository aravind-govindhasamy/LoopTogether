package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlowCard
import com.example.ui.theme.*
import com.example.viewmodel.LoopTogetherViewModel

import com.example.ui.components.frostedGlassBackground

@Composable
fun LoginScreen(viewModel: LoopTogetherViewModel) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf("🎧") }
    
    val avatarOptions = listOf("🎧", "🔥", "⚡", "👾", "🎸", "🌌", "🔊", "🦊")
    var isSubmitted by remember { mutableStateOf(false) }
    var showGoogleAuthPicker by remember { mutableStateOf(false) }
    var isVerifyingToken by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .frostedGlassBackground(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Header Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(NeonBlue, NeonPurple))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to LoopTogether",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Join synchronized music tunnels instantly",
                fontSize = 13.sp,
                color = TextSubdued,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Glassmorphism Portal Card
            GlowCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonPurple,
                hasGlow = true
            ) {
                Text(
                    text = "Configure Listening Persona",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonBlue,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Username TextField
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Display Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonPurple) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = DarkBorderNeon,
                        focusedLabelColor = NeonBlue,
                        unfocusedLabelColor = TextSubdued
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                // Email TextField (for simulated Google verification)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Google Email Node") },
                    leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = NeonPurple) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = DarkBorderNeon,
                        focusedLabelColor = NeonBlue,
                        unfocusedLabelColor = TextSubdued
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Avatar Selector
                Text(
                    text = "Select Cosmic Symbol",
                    fontSize = 12.sp,
                    color = TextSubdued,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    avatarOptions.forEach { avatar ->
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedAvatar == avatar) NeonPurple.copy(alpha = 0.3f)
                                    else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selectedAvatar == avatar) NeonPurple else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedAvatar = avatar },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = avatar, fontSize = 20.sp)
                        }
                    }
                }

                // Google sign-in action button
                Button(
                    onClick = {
                        if (username.isBlank() || email.isBlank()) {
                            username = "Aravinda PG"
                            email = "aravindapg06@gmail.com"
                        }
                        showGoogleAuthPicker = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonPurple,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Sign In Action",
                        tint = NeonBlue,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Sign In via Google OAuth",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Google OAuth Verification Account Chooser Bottom Dialog
            if (showGoogleAuthPicker) {
                AlertDialog(
                    onDismissRequest = { 
                        if (!isVerifyingToken) showGoogleAuthPicker = false 
                    },
                    containerColor = Color(0xFF0C0C14),
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Google",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Choose an account to continue to LoopTogether",
                                color = TextSubdued,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    },
                    text = {
                        if (isVerifyingToken) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = NeonBlue)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Verifying secure OAuth credentials node...",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Establishing low-latency stream capability",
                                    color = TextSubdued,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Divider(color = Color.White.copy(alpha = 0.1f))
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isVerifyingToken = true }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = selectedAvatar, fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = username.ifBlank { "Aravinda PG" }, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(text = email.ifBlank { "aravindapg06@gmail.com" }, color = TextSubdued, fontSize = 12.sp)
                                    }
                                    Text("✅", fontSize = 16.sp)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            username = "Guest Looper"
                                            email = "guest@looptogether.com"
                                            selectedAvatar = "🦊"
                                            isVerifyingToken = true
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "🦊", fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Guest Looper", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "guest@looptogether.com", color = TextSubdued, fontSize = 12.sp)
                                    }
                                    Text("🔗", fontSize = 16.sp)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            username = "Dev Sonic"
                                            email = "developer@looptogether.com"
                                            selectedAvatar = "👾"
                                            isVerifyingToken = true
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "👾", fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Dev Sonic", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "developer@looptogether.com", color = TextSubdued, fontSize = 12.sp)
                                    }
                                    Text("🔗", fontSize = 16.sp)
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        if (!isVerifyingToken) {
                            TextButton(onClick = { showGoogleAuthPicker = false }) {
                                Text("Cancel", color = TextSubdued)
                            }
                        }
                    }
                )

                if (isVerifyingToken) {
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(1600)
                        isVerifyingToken = false
                        showGoogleAuthPicker = false
                        viewModel.proceedLogin(username, email, selectedAvatar)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Safety Warning notice as strictly required by 'gemini-api' skill / general practices
            Text(
                text = "Secured via persistent OAuth node environment simulation.\nFull compliance with low-latency listening schemas.",
                fontSize = 10.sp,
                color = TextSubdued.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}
