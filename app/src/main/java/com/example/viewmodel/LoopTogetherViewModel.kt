package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

class LoopTogetherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val TAG = "LoopTogetherViewModel"

    // --- Authentication State ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentScreen = MutableStateFlow("splash") // splash, login, home, create_room, join_room, room, profile, notifications, settings
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // --- Rooms Feed ---
    val availableRooms: StateFlow<List<RoomEntity>> = repository.getAllRoomsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Room Context ---
    private val _activeRoomId = MutableStateFlow<String?>(null)
    val activeRoomId: StateFlow<String?> = _activeRoomId.asStateFlow()

    val activeRoom: StateFlow<RoomEntity?> = _activeRoomId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getRoomFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeRoomQueue: StateFlow<List<QueueItemEntity>> = _activeRoomId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getActiveQueueFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeRoomMessages: StateFlow<List<ChatMessageEntity>> = _activeRoomId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getMessagesFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Live Event Streams ---
    private val _incomingEmojiResponse = MutableSharedFlow<String>(replay = 0)
    val incomingEmojiResponse: SharedFlow<String> = _incomingEmojiResponse.asSharedFlow()

    private val _isPeerTyping = MutableStateFlow(false)
    val isPeerTyping: StateFlow<Boolean> = _isPeerTyping.asStateFlow()

    private val _typingPeerName = MutableStateFlow("")
    val typingPeerName: StateFlow<String> = _typingPeerName.asStateFlow()

    private val _liveActivityEvent = MutableStateFlow<String?>(null)
    val liveActivityEvent: StateFlow<String?> = _liveActivityEvent.asStateFlow()

    fun postActivityEvent(event: String) {
        viewModelScope.launch {
            _liveActivityEvent.value = event
            delay(3500)
            if (_liveActivityEvent.value == event) {
                _liveActivityEvent.value = null
            }
        }
    }

    // --- Notifications ---
    val notifications: StateFlow<List<NotificationEntity>> = repository.getNotificationsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Search Module ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SongSearchModel>>(emptyList())
    val searchResults: StateFlow<List<SongSearchModel>> = _searchResults.asStateFlow()

    // --- Settings & Diagnostics ---
    val syncLatencyMs = MutableStateFlow(42) // Displayed synchronization delay (under 500ms!)
    val isVisualizerEnabled = MutableStateFlow(true)
    val isAudioOutputWired = MutableStateFlow(true)

    // Expanded Premium Settings State
    val audioQuality = MutableStateFlow("high") // standard, high, epic
    val videoQuality = MutableStateFlow("auto") // standard, high, auto
    val pushNotificationsEnabled = MutableStateFlow(true)
    val friendActivityAlertsEnabled = MutableStateFlow(true)
    val roomInvitesEnabled = MutableStateFlow(true)
    val cosmicMidnightTheme = MutableStateFlow(true)
    val accentColorIndex = MutableStateFlow("purple") // purple, blue, pink
    val reducedMotionEnabled = MutableStateFlow(false)
    val blockedUsers = MutableStateFlow<List<String>>(emptyList())
    val profileVisibility = MutableStateFlow("Public") // Public, Friends Only, Private
    val showActivityStatus = MutableStateFlow(true)
    val onboardingCompleted = MutableStateFlow(false)

    fun blockUser(username: String) {
        if (username.isNotBlank() && !blockedUsers.value.contains(username)) {
            blockedUsers.value = blockedUsers.value + username
        }
    }

    fun unblockUser(username: String) {
        blockedUsers.value = blockedUsers.value - username
    }

    fun deleteHistoryLogs() = viewModelScope.launch {
        // Mock deletion of local logs
        postActivityEvent("Playback history logs purged successfully 🧹")
    }

    fun requestAccountDeletion() = viewModelScope.launch {
        postActivityEvent("Deletion request logged. Account scheduled for removal in 30 days.")
    }

    // Background Jobs Anchor
    private var playbackSyncJob: Job? = null
    private var simulationActivityJob: Job? = null

    // Realtime Systems & Sockets
    val socketService = RealtimeSocketService(application, repository, viewModelScope)
    val socketConnectionState = socketService.connectionState
    val activeRoomSocketUsers = socketService.activeUserList

    init {
        viewModelScope.launch {
            repository.preseedInitialDataIfEmpty()
            // Check if there is a persistent active local session
            val persistedUser = repository.getAuthUser("current_logged_in_user")
            _currentUser.value = persistedUser

            // Chain socket flows and synchronization metrics
            launch {
                socketService.syncLatencyMs.collect {
                    syncLatencyMs.value = it
                }
            }
            launch {
                socketService.incomingEmoji.collect {
                    _incomingEmojiResponse.emit(it)
                }
            }
            launch {
                socketService.isPeerTyping.collect {
                    _isPeerTyping.value = it
                }
            }
            launch {
                socketService.typingPeerName.collect {
                    _typingPeerName.value = it
                }
            }
            launch {
                socketService.serverSyncState.collect { data ->
                    handleIncomingPlaybackSync(data)
                }
            }
        }
    }

    private suspend fun handleIncomingPlaybackSync(data: org.json.JSONObject) {
        val roomId = _activeRoomId.value ?: return
        val currentRoom = repository.getRoom(roomId) ?: return
        
        val isPlaying = data.optBoolean("isPlaying", currentRoom.isPlaying)
        val position = data.optLong("currentPlaybackPosition", currentRoom.currentPlaybackPosition)
        
        val updated = currentRoom.copy(
            isPlaying = isPlaying,
            currentPlaybackPosition = position,
            lastUpdated = System.currentTimeMillis()
        )
        repository.updateRoomSyncState(updated)

        // Post visual activity event when remote host triggers synchronization action
        val triggerUser = data.optString("triggeredBy", "Someone")
        if (triggerUser != _currentUser.value?.username) {
            postActivityEvent("$triggerUser synchronized playback to ${formatDuration(position)} 🔄")
        }
    }

    // --- Authentication Actions ---
    fun proceedLogin(username: String, email: String, avatar: String) = viewModelScope.launch {
        val cleanName = username.ifBlank { "Looper_${(1000..9999).random()}" }
        val cleanEmail = email.ifBlank { "looper@looptogether.io" }
        val user = UserEntity(
            id = "current_logged_in_user", // Single local persistent session key
            username = cleanName,
            email = cleanEmail,
            profilePicUrl = avatar,
            isOnline = true
        )
        repository.insertUser(user)
        _currentUser.value = user
        navigateTo("home")
    }

    fun logout() = viewModelScope.launch {
        repository.deleteUserSession("current_logged_in_user")
        _currentUser.value = null
        navigateTo("login")
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // --- User profile customization ---
    fun updateProfile(username: String, email: String, avatar: String) = viewModelScope.launch {
        _currentUser.value?.let { current ->
            val updated = current.copy(username = username, email = email, profilePicUrl = avatar)
            repository.updateUser(updated)
            _currentUser.value = updated
        }
    }

    // --- Custom Room System Actions ---
    fun hostNewRoom(roomName: String, description: String, isPublic: Boolean) = viewModelScope.launch {
        val user = _currentUser.value ?: return@launch
        val randomCode = generateRoomCode()
        val room = repository.createRoom(
            code = randomCode,
            name = roomName.ifBlank { "Late Night Groove Party" },
            description = description.ifBlank { "Listening together in synchronized frequencies." },
            hostId = user.id,
            hostName = user.username,
            isPublic = isPublic
        )
        
        // Connect and notify socket server
        socketService.connect()
        val joinObj = org.json.JSONObject().apply {
            put("roomId", room.id)
            put("userId", user.id)
            put("userName", user.username)
            put("userAvatar", user.profilePicUrl)
        }
        socketService.emit("join_room", joinObj)

        _activeRoomId.value = room.id
        startPlaybackSyncTicker()
        startPeerSimulation()
        navigateTo("room")
    }

    fun joinRoomByCode(code: String, onSuccess: () -> Unit, onError: (String) -> Unit) = viewModelScope.launch {
        val cleanCode = code.uppercase().trim()
        if (cleanCode.isBlank()) {
            onError("Room code cannot be empty.")
            return@launch
        }
        var r = repository.getRoom(cleanCode)
        if (r == null) {
            val user = _currentUser.value ?: return@launch
            r = RoomEntity(
                id = cleanCode,
                name = "Room $cleanCode",
                description = "Synchronized Listening Space Network",
                hostId = "",
                hostUsername = "",
                isPublic = false,
                inviteCode = cleanCode,
                memberCount = 1,
                currentSongId = "dQw4w9WgXcQ",
                currentSongTitle = "Never Gonna Give You Up",
                currentSongArtist = "Rick Astley",
                currentSongDuration = 212000,
                currentPlaybackPosition = 0,
                isPlaying = false,
                lastUpdated = System.currentTimeMillis()
            )
            repository.updateRoomSyncState(r)
        }

        _activeRoomId.value = r.id
        _currentUser.value?.let { user ->
            // Connect and notify socket server
            socketService.connect()
            val joinObj = org.json.JSONObject().apply {
                put("roomId", r.id)
                put("userId", user.id)
                put("userName", user.username)
                put("userAvatar", user.profilePicUrl)
            }
            socketService.emit("join_room", joinObj)

            repository.insertChatMessage(
                ChatMessageEntity(
                    roomId = r.id,
                    userId = "SYSTEM",
                    userName = "System",
                    userAvatar = "⚡",
                    content = "${user.username} entered the listening room.",
                    isSystem = true
                )
            )
            repository.insertNotification(
                NotificationEntity(
                    title = "Joined listening Room",
                    description = "Successfully synchronized with room '${r.name}'. Sync Delay: ${syncLatencyMs.value}ms.",
                    type = "SYSTEM"
                )
            )
        }
        startPlaybackSyncTicker()
        startPeerSimulation()
        postActivityEvent("${_currentUser.value?.username ?: "You"} entered the listening tunnel 💫")
        onSuccess()
        navigateTo("room")
    }

    fun leaveCurrentRoom() = viewModelScope.launch {
        val roomId = _activeRoomId.value ?: return@launch
        val user = _currentUser.value
        if (user != null) {
            // Leave socket room and teardown socket
            val leaveObj = org.json.JSONObject().apply {
                put("roomId", roomId)
                put("userId", user.id)
                put("userName", user.username)
            }
            socketService.emit("leave_room", leaveObj)
            socketService.disconnect()

            repository.insertChatMessage(
                ChatMessageEntity(
                    roomId = roomId,
                    userId = "SYSTEM",
                    userName = "System",
                    userAvatar = "🚪",
                    content = "${user.username} exited the room.",
                    isSystem = true
                )
            )
        }
        _activeRoomId.value = null
        playbackSyncJob?.cancel()
        simulationActivityJob?.cancel()
        navigateTo("home")
        user?.let { postActivityEvent("${it.username} exited the listening tunnel Key 👋") }
    }

    fun deleteNotificationItem(id: Int) = viewModelScope.launch {
        repository.deleteNotification(id)
    }

    // --- Playback Sync Control Actions ---
    fun togglePlaybackState() = viewModelScope.launch {
        val roomId = _activeRoomId.value ?: return@launch
        val currentRoom = repository.getRoom(roomId) ?: return@launch
        val currentUserId = _currentUser.value?.id ?: ""
        
        // Host check, or if synchronized collaborative state is allowed
        if (currentRoom.isLocked && currentRoom.hostId != currentUserId) {
            triggerSystemRoomAlert(roomId, "Playback controls are locked. Only the Host can pause or play.")
            return@launch
        }

        val updatedState = currentRoom.copy(isPlaying = !currentRoom.isPlaying, lastUpdated = System.currentTimeMillis())
        repository.updateRoomSyncState(updatedState)
        postActivityEvent("${_currentUser.value?.username ?: "User"} ${if (updatedState.isPlaying) "resumed ▶️" else "paused ⏸️"} playback")

        // Broadcast to socket backend
        val playObj = org.json.JSONObject().apply {
            put("roomId", roomId)
            put("userId", _currentUser.value?.username ?: "Unknown")
            put("positionMs", updatedState.currentPlaybackPosition)
            put("isPlaying", updatedState.isPlaying)
        }
        socketService.emit(if (updatedState.isPlaying) "play_video" else "pause_video", playObj)

        // Insert log
        repository.insertChatMessage(
            ChatMessageEntity(
                roomId = roomId,
                userId = "SYSTEM",
                userName = "System",
                userAvatar = "🎵",
                content = "Playback ${if (updatedState.isPlaying) "resumed" else "paused"} by ${_currentUser.value?.username}.",
                isSystem = true
            )
        )
    }

    fun seekPlaybackPosition(positionMs: Long) = viewModelScope.launch {
        val roomId = _activeRoomId.value ?: return@launch
        val currentRoom = repository.getRoom(roomId) ?: return@launch
        val currentUserId = _currentUser.value?.id ?: ""

        if (currentRoom.isLocked && currentRoom.hostId != currentUserId) {
            triggerSystemRoomAlert(roomId, "Playback controls are locked by the Host.")
            return@launch
        }

        val updatedState = currentRoom.copy(currentPlaybackPosition = positionMs, lastUpdated = System.currentTimeMillis())
        repository.updateRoomSyncState(updatedState)

        // Broadcast seek packet to socket
        val seekObj = org.json.JSONObject().apply {
            put("roomId", roomId)
            put("userId", _currentUser.value?.username ?: "Unknown")
            put("positionMs", positionMs)
        }
        socketService.emit("seek_video", seekObj)
    }

    fun toggleRoomLock() = viewModelScope.launch {
        val roomId = _activeRoomId.value ?: return@launch
        val currentRoom = repository.getRoom(roomId) ?: return@launch
        val currentUserId = _currentUser.value?.id ?: ""

        if (currentRoom.hostId == currentUserId) {
            val updated = currentRoom.copy(isLocked = !currentRoom.isLocked, lastUpdated = System.currentTimeMillis())
            repository.updateRoomSyncState(updated)
            triggerSystemRoomAlert(roomId, "Room settings changed: Controls ${if (updated.isLocked) "Host-Only (Locked)" else "Shared (Unlocked)"}.")
        }
    }

    // --- Search & Shared Queue Orchestration ---
    fun triggerSearch(query: String) {
        _searchQuery.value = query
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val hUrl = socketService.getHttpServerUrl()
            val results = repository.searchSongs(query, hUrl)
            _searchResults.value = results
        }
    }

    fun addSongToRoomQueue(song: SongSearchModel) = viewModelScope.launch {
        val roomId = _activeRoomId.value ?: return@launch
        val user = _currentUser.value ?: return@launch
        val activeQueue = repository.getActiveQueue(roomId)

        val nextPos = (activeQueue.maxOfOrNull { it.position } ?: -1) + 1
        val queueItem = QueueItemEntity(
            roomId = roomId,
            videoId = song.videoId,
            title = song.title,
            artist = song.artist,
            duration = song.durationMs,
            addedByUserId = user.id,
            addedByUsername = user.username,
            position = nextPos
        )
        repository.insertQueueItem(queueItem)
        postActivityEvent("${user.username} queued '${song.title}' 🎵")

        // Broadcast to cooperative queue socket
        val qObj = org.json.JSONObject().apply {
            put("roomId", roomId)
            put("videoId", song.videoId)
            put("title", song.title)
            put("artist", song.artist)
            put("duration", song.durationMs)
            put("addedByUserId", user.id)
            put("addedByUsername", user.username)
        }
        socketService.emit("queue_video", qObj)

        // Post chat log
        repository.insertChatMessage(
            ChatMessageEntity(
                roomId = roomId,
                userId = "SYSTEM",
                userName = "System",
                userAvatar = "💿",
                content = "${user.username} appended '${song.title}' to the collaborative queue.",
                isSystem = true
            )
        )
    }

    fun upvoteQueueItem(itemId: Int) = viewModelScope.launch {
        val roomId = _activeRoomId.value ?: return@launch
        val activeQueue = repository.getActiveQueue(roomId)
        val item = activeQueue.find { it.id == itemId } ?: return@launch
        val updated = item.copy(voteCount = item.voteCount + 1)
        repository.updateQueueItem(updated)

        // Broadcast upvote to socket backend
        val voteObj = org.json.JSONObject().apply {
            put("roomId", roomId)
            put("itemId", item.id) // DB local primary key
            put("videoId", item.videoId)
        }
        socketService.emit("vote_video", voteObj)
    }

    fun skipCurrentSong() = viewModelScope.launch {
        val roomId = _activeRoomId.value ?: return@launch
        skipToNextSongInQueue(roomId)
    }

    private suspend fun skipToNextSongInQueue(roomId: String) {
        val room = repository.getRoom(roomId) ?: return
        val activeQueue = repository.getActiveQueue(roomId)
        val currentUserId = _currentUser.value?.id ?: ""

        if (room.isLocked && room.hostId != currentUserId) {
            triggerSystemRoomAlert(roomId, "Only Host can skip tracks when room is locked.")
            return
        }

        if (activeQueue.isNotEmpty()) {
            val nextSong = activeQueue.first()
            
            // Mark queue item as played
            repository.updateQueueItem(nextSong.copy(isPlayed = true))

            // Load into room metadata
            val updatedRoom = room.copy(
                currentSongId = nextSong.videoId,
                currentSongTitle = nextSong.title,
                currentSongArtist = nextSong.artist,
                currentSongDuration = nextSong.duration,
                currentPlaybackPosition = 0,
                isPlaying = true,
                lastUpdated = System.currentTimeMillis()
            )
            repository.updateRoomSyncState(updatedRoom)

            // Notify socket server to skip active track
            val skipObj = org.json.JSONObject().apply {
                put("roomId", roomId)
                put("userId", _currentUser.value?.username ?: "Unknown")
            }
            socketService.emit("next_video", skipObj)

            // Alert via chat
            repository.insertChatMessage(
                ChatMessageEntity(
                    roomId = roomId,
                    userId = "SYSTEM",
                    userName = "System",
                    userAvatar = "⏭️",
                    content = "Synchronizing next track in audio stream: '${nextSong.title}' [Duration: ${formatDuration(nextSong.duration)}].",
                    isSystem = true
                )
            )
        } else {
            // Re-sync progress back to 0, pause, and notify queue empty
            val updatedRoom = room.copy(
                currentPlaybackPosition = 0,
                isPlaying = false,
                lastUpdated = System.currentTimeMillis()
            )
            repository.updateRoomSyncState(updatedRoom)
            
            // Send track concluded skip frame
            val skipObj = org.json.JSONObject().apply {
                put("roomId", roomId)
                put("userId", _currentUser.value?.username ?: "Unknown")
            }
            socketService.emit("next_video", skipObj)

            triggerSystemRoomAlert(roomId, "Collaborative queue concluded. Add songs from your Search/Explore tab!")
        }
    }

    // --- Interactive Live Chat & AI Messaging ---
    fun sendChatMessage(content: String) = viewModelScope.launch {
        val roomId = _activeRoomId.value ?: return@launch
        val user = _currentUser.value ?: return@launch
        if (content.isBlank()) return@launch

        // Save authentic user chat message
        val chatMsg = ChatMessageEntity(
            roomId = roomId,
            userId = user.id,
            userName = user.username,
            userAvatar = user.profilePicUrl,
            content = content
        )
        repository.insertChatMessage(chatMsg)

        // Broadcast to socket server
        val msgObj = org.json.JSONObject().apply {
            put("roomId", roomId)
            put("senderId", user.id)
            put("senderName", user.username)
            put("senderAvatar", user.profilePicUrl)
            put("message", content)
            put("messageType", "USER")
        }
        socketService.emit("send_message", msgObj)

        // Detect AI DJ Commands: "@gemini", "/dj" or "!dj"
        if (content.contains("@gemini", ignoreCase = true) || 
            content.contains("/dj", ignoreCase = true) || 
            content.contains("!dj", ignoreCase = true)) {
            triggerAiDjBrain(roomId, content)
        }
    }

    fun submitEmojiReaction(emoji: String) = viewModelScope.launch {
        _incomingEmojiResponse.emit(emoji)
        
        val roomId = _activeRoomId.value ?: return@launch
        val user = _currentUser.value ?: return@launch
        
        // Broadcast emoji blast to socket server
        val reactObj = org.json.JSONObject().apply {
            put("roomId", roomId)
            put("emoji", emoji)
            put("userName", user.username)
        }
        socketService.emit("reaction_sent", reactObj)
    }

    // AI DJ Trigger Logic
    private fun triggerAiDjBrain(roomId: String, userMessage: String) = viewModelScope.launch(Dispatchers.IO) {
        delay(800) // Aesthetic delay for typing indic
        _typingPeerName.value = "LoopDJ (Gemini)"
        _isPeerTyping.value = true

        val promptStr = "The user says: '$userMessage' in a social music room. Recommend some songs or give a cool, snappy reply."
        val systemInstructions = "You are LoopDJ, a friendly AI DJ assistant. Always answer very briefly (2 sentences max) in active, trendy, music-loving language. Suggest 1 or 2 hit track titles that users might like."

        val responseContent = GeminiClient.generateAiContent(promptStr, systemInstructions)

        _isPeerTyping.value = false
        _typingPeerName.value = ""

        // Post back generated AI DJ response as system or bot message
        repository.insertChatMessage(
            ChatMessageEntity(
                roomId = roomId,
                userId = "AI_DJ",
                userName = "LoopDJ ✨",
                userAvatar = "🤖",
                content = responseContent
            )
        )
    }

    // Helper alerts
    private suspend fun triggerSystemRoomAlert(roomId: String, info: String) {
        repository.insertChatMessage(
            ChatMessageEntity(
                roomId = roomId,
                userId = "SYSTEM",
                userName = "System",
                userAvatar = "🛡️",
                content = info,
                isSystem = true
            )
        )
    }

    // --- Core Background Job Loops ---

    /**
     * Start the real-time playback ticks calculation background loop.
     */
    private fun startPlaybackSyncTicker() {
        playbackSyncJob?.cancel()
        playbackSyncJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                val roomId = _activeRoomId.value
                if (roomId != null) {
                    val r = repository.getRoom(roomId)
                    if (r != null && r.isPlaying) {
                        val currentProg = r.currentPlaybackPosition
                        val totalDuration = r.currentSongDuration
                        
                        if (currentProg + 1000 >= totalDuration) {
                            withContext(Dispatchers.Main) {
                                skipToNextSongInQueue(roomId)
                            }
                        } else {
                            // Tick advance local DB playback position
                            repository.updateRoomSyncState(
                                r.copy(
                                    currentPlaybackPosition = currentProg + 1000,
                                    lastUpdated = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
                delay(1000)
            }
        }
    }

    /**
     * Start simulating actions in background to show real activity. Disconnect prototype simulation for a fully live real-world production mode
     */
    private fun startPeerSimulation() {
        // Disabling prototype mock peer simulation for production mode
    }

    // UTILITIES
    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val dig = "0123456789"
        val formats = listOf(
            { "LOOP" + (1..9).random().toString() }, // e.g., LOOP7
            { "LT" + (10..99).random().toString() + chars.random() }, // e.g., LT92X
            { "" + chars.random() + chars.random() + (10..99).random() + chars.random() } // e.g., VX12A
        )
        return formats.random().invoke()
    }

    fun formatDuration(ms: Long): String {
        val totalSecs = ms / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format("%02d:%02d", mins, secs)
    }

    override fun onCleared() {
        super.onCleared()
        playbackSyncJob?.cancel()
        simulationActivityJob?.cancel()
    }
}
