package com.mnn.cameranote.data.local.entity.relations

import androidx.room.Embedded
import com.mnn.cameranote.data.local.entity.MessageEntity

/**
 * 列表页所需的「备忘 + 封面 + 条目数」投影。
 *
 * [content] 是封面图片路径，可能为 null（该备忘还没有照片，只有文字）。
 */
data class MessageWithContent(
    @Embedded val messageEntity: MessageEntity,
    val content: String?,
    val itemCount: Int,
)
