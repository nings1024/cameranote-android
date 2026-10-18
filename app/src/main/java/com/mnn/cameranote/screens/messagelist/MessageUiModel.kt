package com.mnn.cameranote.screens.messagelist

import com.mnn.cameranote.data.local.entity.relations.MessageWithContent

/** 列表项 UI 模型。 */
data class MessageUiModel(
    val id: Long,
    val title: String,
    val location: String,
    val detailInfo: String,
    val createTime: Long,
    /** 封面图片绝对路径，没有照片时为 null */
    val coverPath: String?,
    /** 备忘内条目数 */
    val itemCount: Int,
    /** true = 还在收集、尚未补描述的草稿 */
    val isDraft: Boolean,
)

fun MessageWithContent.toUiModel(): MessageUiModel = MessageUiModel(
    id = messageEntity.id,
    title = messageEntity.title,
    location = messageEntity.location,
    detailInfo = messageEntity.detailInfo,
    createTime = messageEntity.createTime,
    coverPath = content,
    itemCount = itemCount,
    isDraft = messageEntity.isTemp && !messageEntity.isDeleted,
)
