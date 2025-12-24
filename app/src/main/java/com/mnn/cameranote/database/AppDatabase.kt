package com.mnn.cameranote.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mnn.cameranote.database.entity.MessageEntity
import com.mnn.cameranote.database.entity.MessageItemEntity
import com.mnn.cameranote.database.dao.MessageDao

// data/database/AppDatabase.kt
@Database(
    entities = [MessageEntity::class, MessageItemEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() { // 必须继承 RoomDatabase
    abstract fun messageDao(): MessageDao
}