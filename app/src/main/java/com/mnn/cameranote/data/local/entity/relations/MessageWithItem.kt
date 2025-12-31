package com.mnn.cameranote.data.local.entity.relations

import androidx.room.Embedded
import com.mnn.cameranote.data.local.entity.MessageEntity
import com.mnn.cameranote.screens.messagelist.MessageUiModel

data class MessageWithContent(
    @Embedded val messageEntity: MessageEntity,
    val content: String,
) {
    fun toUiModel(): MessageUiModel {
        return MessageUiModel(
            id = messageEntity.id,
            title = messageEntity.title,
            location = messageEntity.location,
            detailInfo = messageEntity.detailInfo,
            createTime = messageEntity.createTime,
            content = content
        )
    }
}
