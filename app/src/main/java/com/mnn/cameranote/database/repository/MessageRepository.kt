package com.mnn.cameranote.data.database.repository

import com.mnn.cameranote.data.database.entity.MessageEntity
import com.mnn.cameranote.data.database.entity.MessageItemEntity
import com.mnn.cameranote.data.database.entity.MessageType
import com.mnn.cameranote.database.dao.MessageDao
import com.mnn.cameranote.model.MessageItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDate

// data/repository/MessageRepository.kt
class MessageRepository(
    private val messageDao: MessageDao, private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun createNewMessage(filePath: String) {
        val currentTime = System.currentTimeMillis()

        val message = MessageEntity(
            title = LocalDate.now().toString(),
            createTime = currentTime,
            location = "Unknown",
            detailInfo = LocalDate.now().toString()
        )
        val item = MessageItemEntity(
            messageId = 0, // 占位，Dao 中会处理
            sequence = 1,
            type = MessageType.IMAGE.ordinal,
            content = filePath,
            createTime = currentTime
        )
        messageDao.createMessageWithDetail(message, item)
    }

    fun selectMessages(): Flow<List<MessageItem>> {
        return messageDao.selectMessages().flowOn(ioDispatcher)
    }

    fun selectMessageById(id: Long): Flow<MessageEntity> {
        return messageDao.selectMessageById(id).flowOn(ioDispatcher)
    }

    fun selectMessageItemById(messageId: Long): Flow<List<MessageItemEntity>> {
        return messageDao.selectMessageItemById(messageId).flowOn(ioDispatcher)
    }

    suspend fun insertOneMessage(messageItemEntity: MessageItemEntity) {
         messageDao.insertMessageItem(messageItemEntity)
    }

    suspend fun updateMessage(title: String, id: Long) {
        messageDao.updateMessage(title, id)
    }

}
