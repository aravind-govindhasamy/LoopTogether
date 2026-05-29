package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ChatMessageEntity
import com.example.data.QueueItemEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.LoopTogetherViewModel

import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.RenderProcessGoneDetail
import com.example.ui.components.frostedGlassBackground

class WebPlaybackBridgeValue(
    private val onStateTick: (isPlaying: Boolean, positionSecs: Double) -> Unit
) {
    @android.webkit.JavascriptInterface
    fun onStateChange(isPlaying: Boolean, currentTime: Double) {
        onStateTick(isPlaying, currentTime)
    }

    @android.webkit.JavascriptInterface
    fun onTick(isPlaying: Boolean, currentTime: Double) {
        onStateTick(isPlaying, currentTime)
    }
}

@Composable
fun YouTubeVideoPlayer(
    videoId: String,
    isPlaying: Boolean,
    playbackPositionMs: Long,
    isHost: Boolean,
    onLocalPlayerChanged: (isPlaying: Boolean, positionMs: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentIsPlaying = rememberUpdatedState(isPlaying)
    val currentPlaybackPositionMs = rememberUpdatedState(playbackPositionMs)
    val currentIsHost = rememberUpdatedState(isHost)
    val currentOnLocalPlayerChanged = rememberUpdatedState(onLocalPlayerChanged)

    val iframeHtml = remember(videoId) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                body, html { margin: 0; padding: 0; width: 100%; height: 100%; background-color: #020205; overflow: hidden; }
                iframe { width: 100%; height: 100%; border: none; }
            </style>
        </head>
        <body>
            <div id="player"></div>
            <script>
                var tag = document.createElement('script');
                tag.src = "https://www.youtube.com/iframe_api";
                var firstScriptTag = document.getElementsByTagName('script')[0];
                firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                var player;
                function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                        height: '100%',
                        width: '100%',
                        videoId: '$videoId',
                        playerVars: {
                            'playsinline': 1,
                            'controls': 1,
                            'disablekb': 0,
                            'fs': 1,
                            'modestbranding': 1,
                            'rel': 0,
                            'showinfo': 0,
                            'autoplay': 1,
                            'mute': 0
                        },
                        events: {
                            'onReady': onPlayerReady,
                            'onStateChange': onPlayerStateChange
                        }
                    });
                }

                function onPlayerReady(event) {
                    player.seekTo(${playbackPositionMs / 1000f}, true);
                    if ($isPlaying) {
                        player.playVideo();
                    } else {
                        player.pauseVideo();
                    }
                }

                function onPlayerStateChange(event) {
                    if (window.AndroidInterface && player && typeof player.getCurrentTime === 'function') {
                        var status = (event.data === 1); // 1 = YT.PlayerState.PLAYING
                        window.AndroidInterface.onStateChange(status, player.getCurrentTime());
                    }
                }

                window.syncPlayback = function(playing, timeSecs) {
                    if (player && typeof player.getPlayerState === 'function') {
                        var state = player.getPlayerState();
                        if (playing) {
                            if (state !== 1) player.playVideo();
                        } else {
                            if (state !== 2) player.pauseVideo();
                        }
                        
                        var diff = Math.abs(player.getCurrentTime() - timeSecs);
                        // Drift Correction Threshold constraint: 4.0s drift to prevent micro-seeking stutter
                        if (diff > 4.0) {
                            player.seekTo(timeSecs, true);
                        }
                    }
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var lastSentIsPlaying by remember { mutableStateOf<Boolean?>(null) }
    var lastSentPositionMs by remember { mutableStateOf(-9999L) }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.let { webView ->
                webView.post {
                    try {
                        webView.removeJavascriptInterface("AndroidInterface")
                        webView.stopLoading()
                        webView.clearHistory()
                        webView.removeAllViews()
                        webView.destroy()
                    } catch (e: Exception) {
                        // Safe exception absorption
                    }
                }
            }
            webViewRef = null
        }
    }

    LaunchedEffect(videoId, isPlaying, playbackPositionMs) {
        webViewRef?.let { webView ->
            // Precision checks: check if standard continuous automatic 1s clock progression ticks
            val timeDiff = kotlin.math.abs(playbackPositionMs - lastSentPositionMs)
            val isNormalProgression = isPlaying && lastSentIsPlaying == true && timeDiff in 700L..1500L
            
            // Sync play state toggles, major seeks (> 3s), or video transitions
            val forceSync = !isNormalProgression && (lastSentIsPlaying != isPlaying || timeDiff > 3000L || lastSentPositionMs < 0)
            
            if (forceSync) {
                lastSentIsPlaying = isPlaying
                lastSentPositionMs = playbackPositionMs
                val seconds = playbackPositionMs / 1000f
                webView.post {
                    try {
                        webView.evaluateJavascript("if (window.syncPlayback) { window.syncPlayback($isPlaying, $seconds); }", null)
                    } catch (e: Exception) {
                        // Safe exception absorption
                    }
                }
            } else if (isNormalProgression) {
                // Keep progress trackers up to date silently without restarting/re-syncing the player
                lastSentIsPlaying = isPlaying
                lastSentPositionMs = playbackPositionMs
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = object : WebViewClient() {
                    override fun onRenderProcessGone(
                        view: WebView?,
                        detail: android.webkit.RenderProcessGoneDetail?
                    ): Boolean {
                        android.util.Log.e("TVP", "WebView renderer process went away. Gracefully returning true to prevent OS termination.")
                        return true
                    }
                }
                settings.apply {
                    javaScriptEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    domStorageEnabled = true
                }
                setBackgroundColor(0xFF020205.toInt())
                
                // Mount JS Bridge for real-time upstream sync reporting
                addJavascriptInterface(
                    WebPlaybackBridgeValue { localPlaying, posSecs ->
                        try {
                            val localPosMs = (posSecs * 1000).toLong()
                            
                            val isPlayingLatest = currentIsPlaying.value
                            val positionMsLatest = currentPlaybackPositionMs.value
                            val isHostLatest = currentIsHost.value
                            val onChangedLatest = currentOnLocalPlayerChanged.value

                            val stateChanged = isPlayingLatest != localPlaying
                            val positionDrifted = kotlin.math.abs(positionMsLatest - localPosMs) > 3000L
                            
                            // Only Host can command authoritative sync actions back to viewModel
                            if (isHostLatest && (stateChanged || positionDrifted)) {
                                post {
                                    onChangedLatest(localPlaying, localPosMs)
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("TVP", "JS Bridge execution failed safely: ${e.message}")
                        }
                    },
                    "AndroidInterface"
                )

                tag = iframeHtml
                loadDataWithBaseURL("https://www.youtube.com", iframeHtml, "text/html", "UTF-8", null)
                webViewRef = this
            }
        },
        update = { webView ->
            webViewRef = webView
            val loadedHtml = webView.tag as? String
            if (loadedHtml != iframeHtml) {
                webView.tag = iframeHtml
                webView.loadDataWithBaseURL("https://www.youtube.com", iframeHtml, "text/html", "UTF-8", null)
            }
        },
        modifier = modifier
    )
}

@Composable
fun RoomScreen(viewModel: LoopTogetherViewModel) {
    val context = LocalContext.current
    
    val user by viewModel.currentUser.collectAsState()
    val room by viewModel.activeRoom.collectAsState()
    val queue by viewModel.activeRoomQueue.collectAsState()
    val messages by viewModel.activeRoomMessages.collectAsState()
    
    val incomingEmojis = viewModel.incomingEmojiResponse
    val peerTyping by viewModel.isPeerTyping.collectAsState()
    val typingPeerName by viewModel.typingPeerName.collectAsState()
    val latency by viewModel.syncLatencyMs.collectAsState()
    val activeSocketUsers by viewModel.activeRoomSocketUsers.collectAsState()

    var activeTab by remember { mutableStateOf("chat") } // chat, queue, settings
    var chatMessageInput by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotate")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    if (room == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NeonPurple)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .frostedGlassBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // --- 1. TOP SECTION (Header controls & sync latency indicator) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.leaveCurrentRoom() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Leave Screen", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = room?.name ?: "Group Listening",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(ActiveGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LATENCY: ${latency}MS • SYNC SECURE",
                                fontSize = 10.sp,
                                color = ActiveGreen,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // Copy invitation code button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSpaceSurface)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Room Code", room?.id)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Room Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = room?.id ?: "JOIN-00",
                                color = NeonBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Scrollable Content
            var playerViewMode by remember { mutableStateOf("video") }
            val currentProgress = room?.currentPlaybackPosition ?: 0L
            val totalDuration = room?.currentSongDuration ?: 180000L
            val progressPercent = if (totalDuration > 0) currentProgress.toFloat() / totalDuration else 0f

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                // Segmented View Mode switch (handcrafted & visual)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("vinyl" to "📀 Vinyl Visuals", "video" to "📺 Synced Video Player").forEach { mode ->
                        val isSelected = playerViewMode == mode.first
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonBlue.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { playerViewMode = mode.first }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.second,
                                color = if (isSelected) NeonBlue else TextSubdued,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (playerViewMode == "video") {
                    val isHost = room?.hostId == user?.id
                    YouTubeVideoPlayer(
                        videoId = room?.currentSongId ?: "dQw4w9WgXcQ",
                        isPlaying = room?.isPlaying == true,
                        playbackPositionMs = currentProgress,
                        isHost = isHost,
                        onLocalPlayerChanged = { playing, posMs ->
                            if (room?.isPlaying != playing) {
                                viewModel.togglePlaybackState()
                            }
                            if (kotlin.math.abs((room?.currentPlaybackPosition ?: 0L) - posMs) > 2000L) {
                                        viewModel.seekPlaybackPosition(posMs)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    // --- 2. CENTER PLAYER MODULE (Album rotating art + title + controls) ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Visualizer Waves backing
                        AnimatedSineVisualizer(
                            isPlaying = room?.isPlaying == true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .align(Alignment.BottomCenter),
                            songTitle = room?.currentSongTitle ?: ""
                        )

                        // Rotating Vinyl Artwork container
                        Box(
                            modifier = Modifier
                                .size(190.dp)
                                .shadow(
                                    elevation = if (room?.isPlaying == true) 18.dp else 4.dp,
                                    shape = CircleShape,
                                    ambientColor = NeonPurple,
                                    spotColor = NeonBlue
                                )
                                .border(
                                    width = 3.dp,
                                    brush = Brush.sweepGradient(listOf(NeonPurple, NeonBlue, HotPink, NeonPurple)),
                                    shape = CircleShape
                                )
                                .clip(CircleShape)
                                .background(DarkSpaceSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300&auto=format&fit=crop",
                                contentDescription = "Song artwork",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                                    .clip(CircleShape)
                                    .rotate(if (room?.isPlaying == true) rotationAngle else 0f)
                            )

                            // Center pinhole
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(CosmicBackground)
                                    .border(2.dp, NeonBlue, CircleShape)
                            )
                        }
                    }
                }

                // Song Meta Text
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = room?.currentSongTitle ?: "Unknown Track",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = room?.currentSongArtist ?: "Unknown Artist",
                        color = TextSubdued,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Playback progression scrub node

                Column {
                    Slider(
                        value = progressPercent,
                        onValueChange = { percent ->
                            viewModel.seekPlaybackPosition((percent * totalDuration).toLong())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = NeonBlue,
                            activeTrackColor = NeonBlue,
                            inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = viewModel.formatDuration(currentProgress),
                            fontSize = 11.sp,
                            color = TextSubdued
                        )
                        Text(
                            text = viewModel.formatDuration(totalDuration),
                            fontSize = 11.sp,
                            color = TextSubdued
                        )
                    }
                }

                // Playback Controller deck
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Elegant Multi-Emoji Reaction Quick Board
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkSpaceSurface)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        listOf("❤️", "🔥", "🎵", "😭", "⚡", "👏").forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .clickable { viewModel.submitEmojiReaction(emoji) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 15.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    // Main Play/Pause Button
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(NeonPurple, NeonBlue)))
                            .clickable { viewModel.togglePlaybackState() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (room?.isPlaying == true) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Sync Toggle",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    // Next Skip Button
                    IconButton(
                        onClick = { viewModel.skipCurrentSong() },
                        modifier = Modifier.background(DarkSpaceSurface, CircleShape)
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Skip Track", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- 3. TAB STRIP & INTERACTIVE BOARD (Chat vs Queue vs Settings) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSpaceSurface)
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        Pair("chat", "💬 LoopChat"),
                        Pair("queue", "📋 Playlist Queue"),
                        Pair("settings", "🛡️ Host Deck")
                    ).forEach { tab ->
                        val isSelected = activeTab == tab.first
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonPurple.copy(alpha = 0.35f) else Color.Transparent)
                                .clickable { activeTab = tab.first }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.second,
                                color = if (isSelected) Color.White else TextSubdued,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Active Tab render board
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    when (activeTab) {
                        "chat" -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Scrollable Messages Board
                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    itemsIndexed(messages) { index, message ->
                                        val isConsecutive = index > 0 && 
                                                messages[index - 1].userId == message.userId && 
                                                !messages[index - 1].isSystem && 
                                                !message.isSystem

                                        if (message.isSystem) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        NeonBlue.copy(alpha = 0.05f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = message.userAvatar, fontSize = 12.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = message.content,
                                                    fontSize = 11.sp,
                                                    color = NeonBlue,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        } else if (isConsecutive) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                // 26dp corresponds to 18sp icon + 8dp spacer
                                                Spacer(modifier = Modifier.width(26.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color.White.copy(alpha = 0.05f))
                                                        .border(
                                                            1.dp,
                                                            if (message.userId == "AI_DJ") HotPink.copy(alpha = 0.2f) else Color.Transparent,
                                                            RoundedCornerShape(12.dp)
                                                        )
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = message.content,
                                                        fontSize = 12.sp,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        } else {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Text(
                                                    text = message.userAvatar,
                                                    fontSize = 18.sp,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = message.userName,
                                                        fontSize = 11.sp,
                                                        color = TextSubdued,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(Color.White.copy(alpha = 0.05f))
                                                            .border(
                                                                1.dp,
                                                                if (message.userId == "AI_DJ") HotPink.copy(alpha = 0.2f) else Color.Transparent,
                                                                RoundedCornerShape(12.dp)
                                                            )
                                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(
                                                            text = message.content,
                                                            fontSize = 12.sp,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Typing indicator text
                                AnimatedVisibility(visible = peerTyping) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(HotPink)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "$typingPeerName is humming a beat...",
                                            fontSize = 9.sp,
                                            color = HotPink,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Quick emoji reaction strip & input deck
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = chatMessageInput,
                                        onValueChange = { chatMessageInput = it },
                                        placeholder = { Text("Ask @gemini, say hi, or chat with Loopers!", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            focusedBorderColor = NeonPurple,
                                            unfocusedBorderColor = DarkBorderNeon
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = {
                                            if (chatMessageInput.isNotBlank()) {
                                                viewModel.sendChatMessage(chatMessageInput)
                                                chatMessageInput = ""
                                            }
                                        },
                                        modifier = Modifier.background(NeonPurple, RoundedCornerShape(10.dp))
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = "Send text", tint = Color.White)
                                    }
                                }
                            }
                        }

                        "queue" -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Upcoming Synchronizations", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    TextButton(onClick = { viewModel.navigateTo("explore") }) {
                                        Text("+ Add Track", color = NeonBlue, fontSize = 11.sp)
                                    }
                                }

                                if (queue.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        EmptyStateView(
                                            icon = Icons.Default.QueueMusic,
                                            title = "Queue is Silent & Empty",
                                            description = "There are no tracks scheduled in this tunnel queue. Search dynamic video links and build your sync array!",
                                            actionText = "Browse Explore Feeds",
                                            color = NeonBlue,
                                            onActionClick = {
                                                viewModel.navigateTo("explore")
                                            }
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(queue) { item ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(DarkSpaceSurface)
                                                    .padding(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(item.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                                        Text("${item.artist} • By ${item.addedByUsername}", color = TextSubdued, fontSize = 10.sp)
                                                    }
                                                    
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "${item.voteCount} votes",
                                                            fontSize = 11.sp,
                                                            color = HotPink,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        IconButton(onClick = { viewModel.upvoteQueueItem(item.id) }, modifier = Modifier.size(24.dp)) {
                                                            Icon(Icons.Default.ThumbUp, contentDescription = null, tint = HotPink, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "settings" -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = DarkSpaceSurface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text("Group Sync Controls (Host Only)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Lock Room Playback", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("Only Host can pause/play/skip tracks", color = TextSubdued, fontSize = 10.sp)
                                        }
                                        Switch(
                                            checked = room?.isLocked == true,
                                            onCheckedChange = { viewModel.toggleRoomLock() },
                                            colors = SwitchDefaults.colors(checkedThumbColor = NeonPurple)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Low-Latency Audio Sync Engine", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("Keeps playback delay <500ms on active client", color = TextSubdued, fontSize = 10.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(ActiveGreen.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("ONLINE", color = ActiveGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- 4. BOTTOM ACTIVE MEMBERS STRIP ---
                Text("Loopers in Room (${activeSocketUsers.size.coerceAtLeast(1)})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    // Current User first
                    item {
                        val userBreathScale by infiniteTransition.animateFloat(
                            initialValue = 0.95f,
                            targetValue = 1.05f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2500, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "user_breath"
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .graphicsLayer {
                                        scaleX = userBreathScale
                                        scaleY = userBreathScale
                                    }
                                    .clip(CircleShape)
                                    .background(NeonBlue.copy(alpha = 0.15f))
                                    .border(
                                        2.dp,
                                        Brush.sweepGradient(listOf(NeonBlue, NeonPurple, NeonBlue)),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(user?.profilePicUrl ?: "🎧", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("You (Host)", color = NeonBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Dynamically render real other peer users from active socket roster
                    val otherSocketUsers = activeSocketUsers.filter {
                        it.optString("userId") != (user?.id ?: "")
                    }
                    itemsIndexed(otherSocketUsers) { index, peerJson ->
                        val peerName = peerJson.optString("userName", "Listener")
                        val peerAvatar = peerJson.optString("userAvatar", "🎧")
                        val peerBreathScale by infiniteTransition.animateFloat(
                            initialValue = 0.94f,
                            targetValue = 1.06f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1800 + (index * 250), easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "peer_breath_$index"
                        )
                        
                        val isTyping = peerTyping && typingPeerName == peerName
                        
                        // Pick glowing ring colors based on status and index
                        val borderGlow = if (room?.isPlaying == true) {
                            if (index % 2 == 0) Brush.sweepGradient(listOf(HotPink, NeonPurple, HotPink))
                            else Brush.sweepGradient(listOf(NeonBlue, NeonPurple, NeonBlue))
                        } else {
                            Brush.sweepGradient(listOf(Color.White.copy(alpha = 0.4f), Color.Transparent))
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .graphicsLayer {
                                        scaleX = peerBreathScale
                                        scaleY = peerBreathScale
                                    }
                                    .clip(CircleShape)
                                    .background(DarkSpaceSurface)
                                    .border(2.dp, borderGlow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(peerAvatar, fontSize = 20.sp)
                                
                                // Glowing active listening pulse halo overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(2.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isTyping) {
                                    // Animated micro waveform soundwave
                                    AnimatedEqualizer(
                                        isPlaying = true,
                                        color = HotPink,
                                        barCount = 3,
                                        barWidth = 2.dp,
                                        maxHeight = 8.dp,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                } else {
                                    // Mini green dot
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(ActiveGreen)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = peerName,
                                    color = if (isTyping) HotPink else Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = if (isTyping) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- FLOATING EMJI PARTICLES overlay canvas ---
        FloatingEmojiCanvas(
            reactionsFlow = incomingEmojis,
            modifier = Modifier.fillMaxSize()
        )

        // --- TOP-SLIDING LIVE ACTIVITY SIGNAL FEED TOAST ---
        val liveEvent by viewModel.liveActivityEvent.collectAsState()
        androidx.compose.animation.AnimatedVisibility(
            visible = liveEvent != null,
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { -it }) + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }) + androidx.compose.animation.fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            liveEvent?.let { text ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(NeonPurple, NeonBlue)),
                            RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xF209090E))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NeonBlue)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = text,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
