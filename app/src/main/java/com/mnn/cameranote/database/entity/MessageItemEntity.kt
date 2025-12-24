package com.mnn.cameranote.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "message_items",
)
data class MessageItemEntity(
    @PrimaryKey(autoGenerate = true) val itemId: Long = 0,
    val messageId: Long,        // 关联主表的ID
    val sequence: Int,          // 序号
    val type: Int,           // 类型
    val content: String,         // 内容
    val createTime: Long,      // 创建时间 (建议存时间戳)
    val isDeleted: Int = 0

)

enum class MessageType(i: Int) {
    TEXT(0), IMAGE(1), VIDEO(2)
}