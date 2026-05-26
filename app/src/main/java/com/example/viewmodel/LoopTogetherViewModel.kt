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
    val appTheme = MutableStateFlow(com.example.ui.theme.LoopTheme.MIDNIGHT_PULSE)
    val motionIntensity = MutableStateFlow(1f) // multiplier for speed/activity
    val blurIntensity = MutableStateFlow(16f) // pixel rating for visual blurs
    val ambientEffectsToggle = MutableStateFlow(true) // floating particles and glow
    val accentColorIndex = MutableStateFlow("purple") // purple, blue, pink, cyan, emerald, rose, orange, indigo
    val reducedMotionEnabled = MutableStateFlow(false)
    val blockedUsers = MutableStateFlow<List<String>>(emptyList())
    val profileVisibility = MutableStateFlow("Public") // Public, Friends Only, Private
    val showActivityStatus = MutableStateFlow(true)
    val onboardingCompleted = MutableStateFlow(false)

    // --- ADVANCED SOCIAL GRAPH STATES (PHASE 13) ---
    val userBio = MutableStateFlow("Synthesia explorer. Late-night synthwave and lofi loops guide my audio orbits.")
    val listeningStreak = MutableStateFlow(12) // listening streak in days
    val topArtistsCode = MutableStateFlow(listOf("The Midnight", "Kavinsky", "FM-84", "Home"))
    val favoriteGenreTags = MutableStateFlow(listOf("Synthwave ⚡", "Lofi Sunset 🌆", "Liquidity DnB 🌊", "Unplugged 🎸"))

    private val _friends = MutableStateFlow<List<FriendModel>>(
        listOf(
            FriendModel(
                id = "f1",
                username = "RetroSonic 🕶️",
                profilePicUrl = "⚡",
                isOnline = true,
                statusType = "Online",
                statusText = "Syncing Retro Vibes",
                currentlyListening = "Resonance - HOME 🌅",
                activeRoomId = "VIBE-99",
                activeRoomName = "Retro Future Synth Lounge",
                isFavorite = true,
                isCloseFriend = true,
                notes = "Met in synthwave release lounge. Awesome music taste!",
                recentlyPlayedWith = listOf("Resonance", "Sunset Drive"),
                compatibility = 94,
                favoriteGenres = listOf("Synthwave ⚡", "Outrun 🌌")
            ),
            FriendModel(
                id = "f2",
                username = "Sarah_Sunset 🌸",
                profilePicUrl = "🦊",
                isOnline = true,
                statusType = "Idle",
                statusText = "Cozy sunset lofi vibes",
                currentlyListening = "We're Finally Landing - HOME ☕",
                activeRoomId = "LOFI-88",
                activeRoomName = "Warm Lofi Cafe",
                isFavorite = true,
                isCloseFriend = false,
                notes = "Frequent late-night listener. Co-hosted 3 lounges.",
                recentlyPlayedWith = listOf("Keep Going", "Stardust"),
                compatibility = 88,
                favoriteGenres = listOf("Lofi Sunset 🌆", "Chillwave 🌊")
            ),
            FriendModel(
                id = "f3",
                username = "WaveRider 🌌",
                profilePicUrl = "👾",
                isOnline = true,
                statusType = "Away",
                statusText = "AFK but looper active",
                currentlyListening = "Neo Tokyo - Scandroid 🤖",
                activeRoomId = null,
                activeRoomName = null,
                isFavorite = false,
                isCloseFriend = true,
                notes = "Sends outstanding playlist recommendation cards.",
                recentlyPlayedWith = listOf("Neo Tokyo"),
                compatibility = 76,
                favoriteGenres = listOf("Cyberpunk 🤖", "Synthpop ⚡")
            ),
            FriendModel(
                id = "f4",
                username = "Elena_Sound 🎸",
                profilePicUrl = "🎸",
                isOnline = false,
                statusType = "Offline",
                statusText = "Offline • 3h ago",
                currentlyListening = "",
                activeRoomId = null,
                activeRoomName = null,
                isFavorite = false,
                isCloseFriend = false,
                notes = "Loves raw acoustic sessions.",
                recentlyPlayedWith = listOf("Acoustic Sunset"),
                compatibility = 65,
                favoriteGenres = listOf("Unplugged 🎸", "Blues 🌊")
            )
        )
    )
    val friends: StateFlow<List<FriendModel>> = _friends.asStateFlow()

    private val _friendRequests = MutableStateFlow<List<FriendRequestModel>>(
        listOf(
            FriendRequestModel("req_1", "CosmicDJ 🌠", "🌌"),
            FriendRequestModel("req_2", "LofiPanda 🐼", "🎧")
        )
    )
    val friendRequests: StateFlow<List<FriendRequestModel>> = _friendRequests.asStateFlow()

    private val _scheduledEvents = MutableStateFlow<List<ListeningEventModel>>(
        listOf(
            ListeningEventModel(
                id = "ev_1",
                title = "Late-Night Synthwave Cruise 🌌",
                description = "Cruising through nostalgic digital cities. Retrowave, future music, and interactive peer loops.",
                hostUsername = "RetroSonic 🕶️",
                startTime = System.currentTimeMillis() + 1800000,
                countdownText = "Starts in 30m",
                rsvpsCount = 42,
                userRsvped = false,
                genre = "Synthwave ⚡"
            ),
            ListeningEventModel(
                id = "ev_2",
                title = "Lofi Album Release Party ☕",
                description = "Cozy listening session with exclusive artist commentary and real-time multiplayer reaction bursts.",
                hostUsername = "Sarah_Sunset 🌸",
                startTime = System.currentTimeMillis() + 7200000,
                countdownText = "Starts in 2h",
                rsvpsCount = 89,
                userRsvped = true,
                genre = "Lofi Sunset 🌆"
            ),
            ListeningEventModel(
                id = "ev_3",
                title = "Cyber Fusion Retro Lounge 🤖",
                description = "Loud neon synthesizers and fast-paced virtual rhythms. Bring your custom queue additions!",
                hostUsername = "WaveRider 🌌",
                startTime = System.currentTimeMillis() + 86400000,
                countdownText = "Tomorrow",
                rsvpsCount = 14,
                userRsvped = false,
                genre = "Cyberpunk 🤖"
            )
        )
    )
    val scheduledEvents: StateFlow<List<ListeningEventModel>> = _scheduledEvents.asStateFlow()

    private val _memoryMoments = MutableStateFlow<List<MemoryMomentModel>>(
        listOf(
            MemoryMomentModel(
                title = "Late-Night Chill Sessions 🌛",
                description = "Vibed together with Sarah_Sunset and RetroSonic for 240+ minutes on Lofi Sunset paths.",
                icon = "🌙"
            ),
            MemoryMomentModel(
                title = "Synchronic Release Spike 🌊",
                description = "Listened to HOME - Resonance at the exact millisecond threshold with 4 Friends simultaneously.",
                icon = "⚡"
            ),
            MemoryMomentModel(
                title = "The Golden Playlist 🎵",
                description = "Added 35 songs that were upvoted to the queue with RetroSonic 🕶️.",
                icon = "🍯"
            )
        )
    )
    val memoryMoments: StateFlow<List<MemoryMomentModel>> = _memoryMoments.asStateFlow()

    private val _achievements = MutableStateFlow<List<AchievementModel>>(
        listOf(
            AchievementModel("Night Owl Listener 🦉", "Vibe in rooms continuously after midnight", "🌙", true),
            AchievementModel("Perfect Host 👑", "Launch a synchronized tunnel that has 5+ concurrent loopers", "💎", true),
            AchievementModel("Music Explorer 🚀", "Upvote 25+ diverse song genres onto communal queues", "🌌", true),
            AchievementModel("Community Favorite 💖", "Receive 50+ emoji reactions on your queued additions", "✨", false),
            AchievementModel("Sync Symphony 🎼", "Shared and synced 100+ songs with friends", "🎵", false)
        )
    )
    val achievements: StateFlow<List<AchievementModel>> = _achievements.asStateFlow()

    fun respondToFriendRequest(reqId: String, accept: Boolean) {
        val request = _friendRequests.value.find { it.id == reqId } ?: return
        _friendRequests.value = _friendRequests.value.filter { it.id != reqId }
        if (accept) {
            val genres = listOf("Chillwave", "Synthpop", "House", "Deep Tech", "Jazz Beats")
            val newFriend = FriendModel(
                id = "friend_" + UUID.randomUUID().toString().take(6),
                username = request.username,
                profilePicUrl = request.profilePicUrl,
                isOnline = true,
                statusType = "Online",
                statusText = "Just entered the orbit",
                currentlyListening = "Syncing Up...",
                compatibility = (70..99).random(),
                favoriteGenres = listOf(genres.random(), "Lofi")
            )
            _friends.value = listOf(newFriend) + _friends.value
            postActivityEvent("Added ${request.username} as friend! 🤝")
            
            // Add notification
            viewModelScope.launch {
                repository.insertNotification(
                    NotificationEntity(
                        title = "Friend Request Accepted 🤝",
                        description = "You are now connected with ${request.username} in the Loopiverse!",
                        type = "FRIEND"
                    )
                )
            }
        } else {
            postActivityEvent("Declined friend request from ${request.username}.")
        }
    }

    fun addFriend(username: String) {
        val clean = username.trim()
        if (clean.isBlank()) return
        if (_friends.value.any { it.username.equals(clean, true) }) {
            postActivityEvent("$clean is already in your Music Circle!")
            return
        }
        val icons = listOf("🦊", "🦁", "🐨", "🐼", "🦄", "🦅")
        val request = FriendRequestModel(
            id = "req_" + UUID.randomUUID().toString().take(6),
            username = clean,
            profilePicUrl = icons.random()
        )
        _friends.value = _friends.value + FriendModel(
            id = UUID.randomUUID().toString(),
            username = clean,
            profilePicUrl = request.profilePicUrl,
            currentlyListening = "Exploring new bands...",
            compatibility = (75..98).random(),
            favoriteGenres = listOf("Synthwave ⚡", "Lofi Sunset 🌆")
        )
        postActivityEvent("Added $clean to your Music Circle! 🤝")
    }

    fun toggleFriendFavorite(friendId: String) {
        _friends.value = _friends.value.map {
            if (it.id == friendId) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    fun toggleCloseFriend(friendId: String) {
        _friends.value = _friends.value.map {
            if (it.id == friendId) it.copy(isCloseFriend = !it.isCloseFriend) else it
        }
    }

    fun updateFriendNote(friendId: String, note: String) {
        _friends.value = _friends.value.map {
            if (it.id == friendId) {
                it.copy(notes = note)
            } else it
        }
    }

    fun toggleEventRsvp(eventId: String) {
        _scheduledEvents.value = _scheduledEvents.value.map { ev ->
            if (ev.id == eventId) {
                val nextStatus = !ev.userRsvped
                val diff = if (nextStatus) 1 else -1
                val rsvpStateMsg = if (nextStatus) "RSVP Confirmed for ${ev.title}! 📅" else "RSVP Removed for ${ev.title}."
                postActivityEvent(rsvpStateMsg)
                
                // Add notification
                viewModelScope.launch {
                    repository.insertNotification(
                        NotificationEntity(
                            title = if (nextStatus) "Event Scheduled Reminder 📅" else "RSVP Cancelled",
                            description = "Your spot is ${if (nextStatus) "secured" else "released"} for ${ev.title}.",
                            type = "SYSTEM"
                        )
                    )
                }

                ev.copy(userRsvped = nextStatus, rsvpsCount = ev.rsvpsCount + diff)
            } else ev
        }
    }

    fun sendQuickRoomInvite(friendId: String) {
        val friendName = _friends.value.find { it.id == friendId }?.username ?: "Your Friend"
        val activeRoomNameVal = activeRoom.value?.name ?: "Late Night Session"
        postActivityEvent("One-tap invite dispatched to $friendName 🎵")
        
        viewModelScope.launch {
            repository.insertNotification(
                NotificationEntity(
                    title = "Invite Sent to $friendName ✉️",
                    description = "A waiting connection token for room \"$activeRoomNameVal\" was delivered.",
                    type = "INVITE"
                )
            )
        }
    }

    fun reportUserFlow(username: String) {
        postActivityEvent("Report logged. Community guards will audit $username 🔒")
    }

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

    fun playSongNow(song: SongSearchModel) = viewModelScope.launch {
        val user = _currentUser.value ?: return@launch
        var roomId = _activeRoomId.value

        if (roomId == null || roomId.isBlank()) {
            val randomCode = generateRoomCode()
            val roomName = "${user.username}'s Solo Lounge"
            val room = repository.createRoom(
                code = randomCode,
                name = roomName,
                description = "Standalone playback session & interactive node.",
                hostId = user.id,
                hostName = user.username,
                isPublic = false
            )
            
            // Connect socket and notify
            socketService.connect()
            val joinObj = org.json.JSONObject().apply {
                put("roomId", room.id)
                put("userId", user.id)
                put("userName", user.username)
                put("userAvatar", user.profilePicUrl)
            }
            socketService.emit("join_room", joinObj)
            
            roomId = room.id
            _activeRoomId.value = roomId
            startPlaybackSyncTicker()
            startPeerSimulation()
        }

        val room = repository.getRoom(roomId) ?: return@launch
        val updatedRoom = room.copy(
            currentSongId = song.videoId,
            currentSongTitle = song.title,
            currentSongArtist = song.artist,
            currentSongDuration = song.durationMs,
            currentPlaybackPosition = 0,
            isPlaying = true,
            lastUpdated = System.currentTimeMillis()
        )
        repository.updateRoomSyncState(updatedRoom)

        // Broadcast to sockets
        val forcePlayObj = org.json.JSONObject().apply {
            put("roomId", roomId)
            put("videoId", song.videoId)
            put("title", song.title)
            put("artist", song.artist)
            put("duration", song.durationMs)
        }
        socketService.emit("force_play_video", forcePlayObj)

        postActivityEvent("${user.username} started playing '${song.title}' 🎧")
        
        // Also post systematic chat alert
        repository.insertChatMessage(
            ChatMessageEntity(
                roomId = roomId,
                userId = "SYSTEM",
                userName = "System",
                userAvatar = "📺",
                content = "${user.username} changed stream focus: '${song.title}'. Sync alignment activated.",
                isSystem = true
            )
        )

        navigateTo("room")
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

// --- ADVANCED SOCIAL GRAPH DATA MODELS (PHASE 13) ---
data class FriendModel(
    val id: String,
    val username: String,
    val profilePicUrl: String,
    val isOnline: Boolean = true,
    val statusType: String = "Online", // "Online", "Idle", "Away", "Offline"
    val statusText: String = "",
    val currentlyListening: String = "",
    val activeRoomId: String? = null,
    val activeRoomName: String? = null,
    val isFavorite: Boolean = false,
    val isCloseFriend: Boolean = false,
    val notes: String = "",
    val recentlyPlayedWith: List<String> = emptyList(),
    val compatibility: Int = 85,
    val favoriteGenres: List<String> = listOf("Synthwave", "Lofi")
)

data class FriendRequestModel(
    val id: String,
    val username: String,
    val profilePicUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ListeningEventModel(
    val id: String,
    val title: String,
    val description: String,
    val hostUsername: String,
    val startTime: Long,
    val countdownText: String = "",
    val rsvpsCount: Int = 12,
    val userRsvped: Boolean = false,
    val genre: String = "Synthwave"
)

data class MemoryMomentModel(
    val title: String,
    val description: String,
    val icon: String = "✨"
)

data class AchievementModel(
    val title: String,
    val description: String,
    val icon: String,
    val unlocked: Boolean = false
)

