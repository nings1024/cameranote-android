package com.mnn.cameranote.screens.messagelist

data class MessageUiModel(
    val id: Long,
    val title: String,          // 标题
    val location: String,       // 地点
    val detailInfo: String,      // 详细信息（主表描述）
    val createTime: Long,       // 创建时间 (建议存时间戳)
    val content: String
)
