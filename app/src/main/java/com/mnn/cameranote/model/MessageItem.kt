package com.mnn.cameranote.model

data class MessageItem(
    val id: Long = 0,
    val title: String,          // 标题
    val createTime: Long,       // 创建时间 (建议存时间戳)
    val location: String,       // 地点
    val detailInfo: String,      // 详细信息（主表描述）
    val content: String
)
