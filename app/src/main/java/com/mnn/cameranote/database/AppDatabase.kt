package com.mnn.cameranote.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mnn.cameranote.database.dao.MessageDao
import com.mnn.cameranote.data.database.entity.MessageEntity
import com.mnn.cameranote.data.database.entity.MessageItemEntity

// data/database/AppDatabase.kt
@Database(
    entities = [MessageEntity::class, MessageItemEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() { // 必须继承 RoomDatabase
    abstract fun messageDao(): MessageDao
}