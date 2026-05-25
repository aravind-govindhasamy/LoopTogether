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
    val coverUrl: String,
    val publishDate: String = "",
    val viewCount: String = ""
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

    private val httpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun purgeLocalDatabases(roomId: String?) {
        if (roomId != null) {
            chatDao.clearMessagesForRoom(roomId)
            queueDao.clearQueueForRoom(roomId)
        }
    }

    // --- User Profile Session Actions ---
    fun getAuthUserFlow(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)
    suspend fun getAuthUser(userId: String): UserEntity? = userDao.getUserById(userId)
    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)
    suspend fun deleteUserSession(userId: String) = userDao.deleteUserById(userId)

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
            memberCount = 1, // Only the host is present initially
            currentSongId = "dQw4w9WgXcQ", // Default real video
            currentSongTitle = "Never Gonna Give You Up",
            currentSongArtist = "Rick Astley",
            currentSongDuration = 212000,
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
    fun searchSongs(query: String, baseUrl: String): List<SongSearchModel> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return emptyList()
        }

        val cleanBaseUrl = if (baseUrl.endsWith("/")) baseUrl.substring(0, baseUrl.length - 1) else baseUrl
        val url = "$cleanBaseUrl/api/search?q=${java.net.URLEncoder.encode(trimmed, "UTF-8")}"
        val request = okhttp3.Request.Builder()
            .url(url)
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    android.util.Log.e("AppRepository", "Live search returned HTTP ${response.code}")
                    return emptyList()
                }

                val bodyStr = response.body?.string() ?: return emptyList()
                val jsonArray = org.json.JSONArray(bodyStr)
                val results = mutableListOf<SongSearchModel>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    results.add(
                        SongSearchModel(
                            videoId = obj.getString("videoId"),
                            title = obj.getString("title"),
                            artist = obj.getString("artist"),
                            durationMs = obj.getLong("durationMs"),
                            coverUrl = obj.getString("coverUrl"),
                            publishDate = obj.optString("publishDate", "Recently uploaded"),
                            viewCount = obj.optString("viewCount", "Interactive stream")
                        )
                    )
                }
                return results
            }
        } catch (e: Exception) {
            android.util.Log.e("AppRepository", "Live proxy search failed: ${e.message}")
            return emptyList()
        }
    }

    // --- Preseed setup on app launch ---
    suspend fun preseedInitialDataIfEmpty() {
        // Dynamic live mode: fully dynamic - seeding is empty so user populates or joins real sessions
    }
}
