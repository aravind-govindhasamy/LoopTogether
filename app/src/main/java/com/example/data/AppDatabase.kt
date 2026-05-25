package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        RoomEntity::class,
        QueueItemEntity::class,
        ChatMessageEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun roomDao(): RoomDao
    abstract fun queueDao(): QueueDao
    abstract fun chatDao(): ChatDao
    abstract fun notificationDao(): NotificationDao
}
