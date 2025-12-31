package com.mnn.cameranote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(
    tableName = "message_items",
)
data class MessageItemEntity(
    @PrimaryKey(autoGenerate = true) val itemId: Long = 0,
    val messageId: Long,        // 关联主表的ID
    val sequence: Int,          // 序号
    val type: MessageItemType,           // 类型
    val content: String,         // 内容
    val source: MessageItemSource=MessageItemSource.CAMERA,
    val isDeleted: Boolean = false,
    val deletedTime: Long = 0,
    val createTime: Long = System.currentTimeMillis()      // 创建时间 (建议存时间戳)
)

enum class MessageItemType(val value: Int) {
    TEXT(0), IMAGE(1), VIDEO(2);

    companion object {
        fun fromValue(value: Int): MessageItemType {
            return entries.find { it.value == value } ?: TEXT
        }
    }
}

enum class MessageItemSource(val value: Int) {
    CAMERA(0), UPLOAD(1);

    companion object {
        fun fromValue(value: Int): MessageItemSource {
            return entries.find { it.value == value } ?: CAMERA
        }
    }
}


// 2. 创建转换器类
class MessageItemConverters {
    @TypeConverter
    fun fromMessageItemType(type: MessageItemType): Int = type.value

    @TypeConverter
    fun toMessageItemType(value: Int): MessageItemType {
        return MessageItemType.fromValue(value)
    }

    @TypeConverter
    fun fromMessageItemSource(source: MessageItemSource): Int = source.value

    @TypeConverter
    fun toMessageItemSource(value: Int): MessageItemSource {
        return MessageItemSource.fromValue(value)
    }
}