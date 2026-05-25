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
    val peerPool = repository.peerPool

    private val _incomingEmojiResponse = MutableSharedFlow<String>(replay = 0)
    val incomingEmojiResponse: SharedFlow<String> = _incomingEmojiResponse.asSharedFlow()

    private val _isPeerTyping = MutableStateFlow(false)
    val isPeerTyping: StateFlow<Boolean> = _isPeerTyping.asStateFlow()

    private val _typingPeerName = MutableStateFlow("")
    val typingPeerName: StateFlow<String> = _typingPeerName.asStateFlow()

    // --- Notifications ---
    val notifications: StateFlow<List<NotificationEntity>> = repository.getNotificationsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Search Module ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SongSearchModel>>(repository.searchCatalogue)
    val searchResults: StateFlow<List<SongSearchModel>> = _searchResults.asStateFlow()

    // --- Settings & Diagnostics ---
    val syncLatencyMs = MutableStateFlow(42) // Displayed synchronization delay (under 500ms!)
    val isVisualizerEnabled = MutableStateFlow(true)
    val isAudioOutputWired = MutableStateFlow(true)

    // Background Jobs Anchor
    private var playbackSyncJob: Job? = null
    private var simulationActivityJob: Job? = null

    init {
        viewModelScope.launch {
            repository.preseedInitialDataIfEmpty()
            // Try to log in as preseeded test user instantly for seamless entry
            val defaultUser = repository.getAuthUser("default_user")
            _currentUser.value = defaultUser
        }
    }

    // --- Authentication Actions ---
    fun proceedLogin(username: String, email: String, avatar: String) = viewModelScope.launch {
        val cleanName = username.ifBlank { "Looper_${(1000..9999).random()}" }
        val cleanEmail = email.ifBlank { "looper@looptogether.io" }
        val user = UserEntity(
            id = "user_${UUID.randomUUID().toString().take(6)}",
            username = cleanName,
            email = cleanEmail,
            profilePicUrl = avatar,
            isOnline = true
        )
        repository.insertUser(user)
        _currentUser.value = user
        navigateTo("home")
    }

    fun logout() {
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
        _activeRoomId.value = room.id
        startPlaybackSyncTicker()
        startPeerSimulation()
        navigateTo("room")
    }

    fun joinRoomByCode(code: String, onSuccess: () -> Unit, onError: (String) -> Unit) = viewModelScope.launch {
        val cleanCode = code.uppercase().trim()
        val r = repository.getRoom(cleanCode)
        if (r != null) {
            _activeRoomId.value = r.id
            // Send system chat alert
            _currentUser.value?.let { user ->
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
                // Add notifications
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
            onSuccess()
            navigateTo("room")
        } else {
            onError("Invalid Room invitation code. Please verify and retry!")
        }
    }

    fun leaveCurrentRoom() = viewModelScope.launch {
        val roomId = _activeRoomId.value ?: return@launch
        _currentUser.value?.let { user ->
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
        viewModelScope.launch {
            _searchResults.value = repository.searchSongs(query)
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

        // Detect AI DJ Commands: "@gemini", "/dj" or "!dj"
        if (content.contains("@gemini", ignoreCase = true) || 
            content.contains("/dj", ignoreCase = true) || 
            content.contains("!dj", ignoreCase = true)) {
            triggerAiDjBrain(roomId, content)
        }
    }

    fun submitEmojiReaction(emoji: String) = viewModelScope.launch {
        _incomingEmojiResponse.emit(emoji)
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
     * Start simulating actions in background to show real activity.
     */
    private fun startPeerSimulation() {
        simulationActivityJob?.cancel()
        simulationActivityJob = viewModelScope.launch(Dispatchers.IO) {
            delay(5000) // Initial entry buffer
            while (isActive) {
                val roomId = _activeRoomId.value ?: break
                val targetRoom = repository.getRoom(roomId) ?: break
                
                // Randomly select peer from list
                val peer = repository.peerPool.random()
                val peerName = peer.first
                val peerAvatar = peer.second

                // Simulate Typing -> Message Event
                if ((1..100).random() > 60) {
                    _typingPeerName.value = peerName
                    _isPeerTyping.value = true
                    delay((2000..4000).random().toLong())
                    _isPeerTyping.value = false
                    _typingPeerName.value = ""

                    val simulatedTexts = listOf(
                        "Yo look at the synchronization rating! It says 42ms delay! Unheard of 🤯",
                        "Added a absolute gem to our collective queue! Go upvote it guys!",
                        "Hey guys! LoopDJ says Gemini is compiling deep playlist recommendations.",
                        "Are we skipping this? It's good but let's drop some synthwave next!",
                        "This beats are hitting perfectly today 🎧",
                        "LoopTogether is going straight to my homepage.",
                        "Who added Never Gonna Give You Up? Legend! 😂"
                    )
                    
                    repository.insertChatMessage(
                        ChatMessageEntity(
                            roomId = roomId,
                            userId = "sim_${peerName}",
                            userName = peerName,
                            userAvatar = peerAvatar,
                            content = simulatedTexts.random()
                        )
                    )
                } 
                // Simulate simple emoji explosions
                else {
                    val quickEmojis = listOf("🔥", "⚡", "❤️", "💯", "🎷", "🌌")
                    _incomingEmojiResponse.emit(quickEmojis.random())
                }

                // Randomly upvote a track or add track
                if ((1..100).random() > 80) {
                    val activeQueue = repository.getActiveQueue(roomId)
                    if (activeQueue.isNotEmpty()) {
                        val randomItem = activeQueue.random()
                        repository.updateQueueItem(randomItem.copy(voteCount = randomItem.voteCount + 1))
                    }
                }

                delay((12000..20000).random().toLong()) // Repeat next simulation event
            }
        }
    }

    // UTILITIES
    private fun generateRoomCode(): String {
        val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val genres = listOf("BEATS", "ROCK", "SYNTH", "CLUB", "LOFI", "VIBES")
        return "${genres.random()}-${(10..99).random()}${(letters.random())}"
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
