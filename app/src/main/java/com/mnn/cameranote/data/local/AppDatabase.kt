package com.mnn.cameranote.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mnn.cameranote.data.local.dao.MessageDao
import com.mnn.cameranote.data.local.entity.MessageEntity
import com.mnn.cameranote.data.local.entity.MessageItemConverters
import com.mnn.cameranote.data.local.entity.MessageItemEntity

// data/database/AppDatabase.kt
@Database(
    entities = [MessageEntity::class, MessageItemEntity::class],
    version = 1
)
@TypeConverters(MessageItemConverters::class)
abstract class AppDatabase : RoomDatabase() { // 必须继承 RoomDatabase
    abstract fun messageDao(): MessageDao
}