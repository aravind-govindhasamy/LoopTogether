package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID

data class SongSearchModel(
    val videoId: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val coverUrl: String
)

class AppRepository(context: Context) {

    // Database Initialization
    private val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "looptogether_database"
    ).fallbackToDestructiveMigration().build()

    private val userDao = db.userDao()
    private val roomDao = db.roomDao()
    private val queueDao = db.queueDao()
    private val chatDao = db.chatDao()
    private val notificationDao = db.notificationDao()

    // --- Search Catalogue (Simulated YouTube Data API Results) ---
    val searchCatalogue = listOf(
        SongSearchModel("dQw4w9WgXcQ", "Never Gonna Give You Up", "Rick Astley", 212000, "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=150&auto=format&fit=crop"),
        SongSearchModel("4NRXx6caW78", "Blinding Lights", "The Weeknd", 200000, "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=150&auto=format&fit=crop"),
        SongSearchModel("34Na4j8AVgA", "Starboy", "The Weeknd ft. Daft Punk", 230000, "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=150&auto=format&fit=crop"),
        SongSearchModel("8GW6sLrK40k", "Resonance", "HOME", 195000, "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=150&auto=format&fit=crop"),
        SongSearchModel("5qap5aO4i9A", "Lofi Sunset Chill Beat", "Lofi Hip Hop Room", 240000, "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=150&auto=format&fit=crop"),
        SongSearchModel("PT2_F-1esPk", "Closer", "The Chainsmokers ft. Halsey", 244000, "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=150&auto=format&fit=crop"),
        SongSearchModel("TUVcZfQe-Kw", "Levitating", "Dua Lipa", 203000, "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=150&auto=format&fit=crop"),
        SongSearchModel("JGwWNGJdvx8", "Shape of You", "Ed Sheeran", 233000, "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=150&auto=format&fit=crop"),
        SongSearchModel("kTJczUoc26U", "Stay", "The Kid LAROI & Justin Bieber", 140000, "https://images.unsplash.com/photo-1487180142328-054b783fc471?w=150&auto=format&fit=crop"),
        SongSearchModel("PmU_APXg6Zc", "Intro", "The xx", 128000, "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=150&auto=format&fit=crop")
    )

    // --- Predefined Active Members pool ---
    val peerPool = listOf(
        Pair("RetroSonic", "🚀"),
        Pair("BeatMaker", "🎧"),
        Pair("VibeMaster", "🍵"),
        Pair("DJ_Gemini", "🤖"),
        Pair("Looper99", "🎸"),
        Pair("AuraSound", "🌌"),
        Pair("Harmonix", "🎷"),
        Pair("BassDrop", "🔈"),
        Pair("SynthWave", "🌈"),
        Pair("NotesSeeker", "📝")
    )

    // --- User Profile Session Actions ---
    fun getAuthUserFlow(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)
    suspend fun getAuthUser(userId: String): UserEntity? = userDao.getUserById(userId)
    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    // --- Listening Room Operations ---
    fun getAllRoomsFlow(): Flow<List<RoomEntity>> = roomDao.getAllRoomsFlow()
    fun getRoomFlow(roomId: String): Flow<RoomEntity?> = roomDao.getRoomByIdFlow(roomId)
    suspend fun getRoom(roomId: String): RoomEntity? = roomDao.getRoomById(roomId)
    suspend fun deleteRoom(roomId: String) = roomDao.deleteRoomById(roomId)

    suspend fun createRoom(
        code: String,
        name: String,
        description: String,
        hostId: String,
        hostName: String,
        isPublic: Boolean
    ): RoomEntity {
        val newRoom = RoomEntity(
            id = code,
            name = name,
            description = description,
            hostId = hostId,
            hostUsername = hostName,
            isPublic = isPublic,
            inviteCode = code,
            memberCount = (3..7).random(), // Fill active mock members initially
            currentSongId = searchCatalogue.first().videoId,
            currentSongTitle = searchCatalogue.first().title,
            currentSongArtist = searchCatalogue.first().artist,
            currentSongDuration = searchCatalogue.first().durationMs,
            currentPlaybackPosition = 0,
            isPlaying = true,
            lastUpdated = System.currentTimeMillis()
        )
        roomDao.insertRoom(newRoom)

        // Preseed initial messages & queue
        chatDao.insertMessage(
            ChatMessageEntity(
                roomId = code,
                userId = "SYSTEM",
                userName = "System Notification",
                userAvatar = "🤖",
                content = "Room synchronized queue online. Dynamic code: $code.",
                isSystem = true
            )
        )
        
        // Add default queue songs
        searchCatalogue.take(3).forEachIndexed { index, song ->
            queueDao.insertQueueItem(
                QueueItemEntity(
                    roomId = code,
                    videoId = song.videoId,
                    title = song.title,
                    artist = song.artist,
                    duration = song.durationMs,
                    voteCount = if (index == 0) 3 else 1,
                    addedByUserId = "SYSTEM",
                    addedByUsername = "AdminDJ",
                    position = index
                )
            )
        }
        return newRoom
    }

    suspend fun updateRoomSyncState(room: RoomEntity) = roomDao.updateRoom(room)

    // --- Shared Queue Operations ---
    fun getActiveQueueFlow(roomId: String): Flow<List<QueueItemEntity>> = queueDao.getActiveQueueFlow(roomId)
    suspend fun getActiveQueue(roomId: String): List<QueueItemEntity> = queueDao.getActiveQueue(roomId)
    suspend fun insertQueueItem(item: QueueItemEntity) = queueDao.insertQueueItem(item)
    suspend fun updateQueueItem(item: QueueItemEntity) = queueDao.updateQueueItem(item)
    suspend fun deleteQueueItem(itemId: Int) = queueDao.deleteQueueItem(itemId)
    suspend fun clearQueue(roomId: String) = queueDao.clearQueueForRoom(roomId)

    // --- Chat Operations ---
    fun getMessagesFlow(roomId: String): Flow<List<ChatMessageEntity>> = chatDao.getMessagesFlow(roomId)
    suspend fun insertChatMessage(message: ChatMessageEntity) = chatDao.insertMessage(message)

    // --- Notification Operations ---
    fun getNotificationsFlow(): Flow<List<NotificationEntity>> = notificationDao.getAllNotificationsFlow()
    suspend fun insertNotification(notification: NotificationEntity) = notificationDao.insertNotification(notification)
    suspend fun markNotificationsRead() = notificationDao.markAllAsRead()
    suspend fun deleteNotification(id: Int) = notificationDao.deleteNotification(id)

    // --- Search query mapping ---
    fun searchSongs(query: String): List<SongSearchModel> {
        return if (query.isEmpty()) {
            searchCatalogue
        } else {
            searchCatalogue.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true)
            }
        }
    }

    // --- Preseed setup on app launch ---
    suspend fun preseedInitialDataIfEmpty() {
        val testUser = getAuthUser("default_user")
        if (testUser == null) {
            // Seed sample authenticated user
            insertUser(
                UserEntity(
                    id = "default_user",
                    username = "SoundWave_Explorer",
                    email = "loop.user@example.com",
                    profilePicUrl = "🔥",
                    listeningMinutes = 1420
                )
            )

            // Seed default beautiful listening rooms
            val roomsToSeed = listOf(
                RoomEntity(
                    id = "SYNTH-88",
                    name = "Synthwave Sunset Glow 🌅",
                    description = "Outrun beats, warm cyber gradients, and nostalgic driving frequencies. Retro theme.",
                    hostId = "retro_sonic",
                    hostUsername = "RetroSonic",
                    isPublic = true,
                    memberCount = 6,
                    inviteCode = "SYNTH-88",
                    currentSongId = "8GW6sLrK40k",
                    currentSongTitle = "Resonance",
                    currentSongArtist = "HOME",
                    currentSongDuration = 195000,
                    isPlaying = true,
                    lastUpdated = System.currentTimeMillis()
                ),
                RoomEntity(
                    id = "CHILL-77",
                    name = "Late Night Chill Beats ☕",
                    description = "Pour some tea and listen to lofi study rhythms. Smooth relaxed frequencies.",
                    hostId = "beat_maker",
                    hostUsername = "BeatMaker",
                    isPublic = true,
                    memberCount = 9,
                    inviteCode = "CHILL-77",
                    currentSongId = "5qap5aO4i9A",
                    currentSongTitle = "Lofi Sunset Chill Beat",
                    currentSongArtist = "Lofi Hip Hop Room",
                    currentSongDuration = 240000,
                    isPlaying = true,
                    lastUpdated = System.currentTimeMillis()
                ),
                RoomEntity(
                    id = "POP-92",
                    name = "Neon Synth Pop Party 🎆",
                    description = "Upbeat dance tracks, sparkling neon dynamic waves. Join for collaborative playlists!",
                    hostId = "aura_sound",
                    hostUsername = "AuraSound",
                    isPublic = true,
                    memberCount = 4,
                    inviteCode = "POP-92",
                    currentSongId = "4NRXx6caW78",
                    currentSongTitle = "Blinding Lights",
                    currentSongArtist = "The Weeknd",
                    currentSongDuration = 200000,
                    isPlaying = true,
                    lastUpdated = System.currentTimeMillis()
                )
            )

            roomsToSeed.forEach { room ->
                roomDao.insertRoom(room)

                // Seed some initial chat logs so the rooms feel live and warm on tap
                chatDao.insertMessage(
                    ChatMessageEntity(
                        roomId = room.id,
                        userId = room.hostId,
                        userName = room.hostUsername,
                        userAvatar = "🎧",
                        content = "Hey loopers! Welcome to our room. Add your favorite tracks to the queue!"
                    )
                )

                chatDao.insertMessage(
                    ChatMessageEntity(
                        roomId = room.id,
                        userId = "guest_1",
                        userName = "VibeMaster",
                        userAvatar = "🍵",
                        content = "This track is incredibly synchronized, absolutely love the latency!"
                    )
                )

                // Fill custom queues for seed rooms
                queueDao.insertQueueItem(
                    QueueItemEntity(
                        roomId = room.id,
                        videoId = room.currentSongId,
                        title = room.currentSongTitle,
                        artist = room.currentSongArtist,
                        duration = room.currentSongDuration,
                        voteCount = 5,
                        addedByUserId = room.hostId,
                        addedByUsername = room.hostUsername,
                        position = 0,
                        isPlayed = false
                    )
                )

                queueDao.insertQueueItem(
                    QueueItemEntity(
                        roomId = room.id,
                        videoId = "TUVcZfQe-Kw",
                        title = "Levitating",
                        artist = "Dua Lipa",
                        duration = 203000,
                        voteCount = 2,
                        addedByUserId = "guest_1",
                        addedByUsername = "VibeMaster",
                        position = 1,
                        isPlayed = false
                    )
                )
            }

            // Seed some system notices
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "Welcome to LoopTogether!",
                    description = "Explore synchronized music listening rooms, invite friends with room codes, and curate shared queues in real time.",
                    type = "SYSTEM"
                )
            )

            notificationDao.insertNotification(
                NotificationEntity(
                    title = "Lofi Beats Invite 💌",
                    description = "VibeMaster invited you to join 'Late Night Chill Beats ☕'.",
                    type = "INVITE"
                )
            )
        }
    }
}
