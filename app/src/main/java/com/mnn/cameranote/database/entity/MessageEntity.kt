package com.mnn.cameranote.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,          // 标题
    val createTime: Long,       // 创建时间 (建议存时间戳)
    val location: String,       // 地点
    val detailInfo: String,      // 详细信息（主表描述）
    val isDeleted: Int = 0  // 0 表示正常，1 表示已删除
)