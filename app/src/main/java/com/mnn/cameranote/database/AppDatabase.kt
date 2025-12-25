package com.mnn.cameranote.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mnn.cameranote.database.dao.MessageDao
import com.mnn.cameranote.database.entity.MessageEntity
import com.mnn.cameranote.database.entity.MessageItemConverters
import com.mnn.cameranote.database.entity.MessageItemEntity

// data/database/AppDatabase.kt
@Database(
    entities = [MessageEntity::class, MessageItemEntity::class],
    version = 1
)
@TypeConverters(MessageItemConverters::class)
abstract class AppDatabase : RoomDatabase() { // 必须继承 RoomDatabase
    abstract fun messageDao(): MessageDao
}