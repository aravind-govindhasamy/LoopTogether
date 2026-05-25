package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val profilePicUrl: String,
    val isOnline: Boolean = true,
    val currentSongTitle: String = "",
    val listeningMinutes: Int = 0,
    val isSyncMode: Boolean = true
)

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey val id: String, // Room code, e.g., "ROCK-90"
    val name: String,
    val description: String,
    val hostId: String,
    val hostUsername: String,
    val isPublic: Boolean = true,
    val maxUsers: Int = 10,
    val memberCount: Int = 1,
    val inviteCode: String,
    // Playback Sync State
    val currentSongId: String = "dQw4w9WgXcQ", // YouTube Video ID
    val currentSongTitle: String = "Never Gonna Give You Up",
    val currentSongArtist: String = "Rick Astley",
    val currentSongDuration: Long = 212000, // in milliseconds
    val currentPlaybackPosition: Long = 0, // in milliseconds
    val isPlaying: Boolean = false,
    val isLocked: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "queue_items")
data class QueueItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val roomId: String,
    val videoId: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val voteCount: Int = 0,
    val addedByUserId: String,
    val addedByUsername: String,
    val position: Int,
    val isPlayed: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val roomId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val content: String,
    val isSystem: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String // "INVITE", "FRIEND", "SYSTEM"
)
