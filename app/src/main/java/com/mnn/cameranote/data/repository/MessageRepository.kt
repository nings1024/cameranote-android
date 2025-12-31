package com.mnn.cameranote.data.repository

import com.mnn.cameranote.data.local.dao.MessageDao
import com.mnn.cameranote.data.local.entity.MessageEntity
import com.mnn.cameranote.data.local.entity.MessageItemEntity
import com.mnn.cameranote.data.local.entity.MessageItemType
import com.mnn.cameranote.data.local.entity.relations.MessageWithContent
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
            detailInfo = LocalDate.now().toString(),
            isTemp = true,
            year = LocalDate.now().year,
            month = LocalDate.now().monthValue,
            day = LocalDate.now().dayOfMonth
        )
        val item = MessageItemEntity(
            messageId = 0, // 占位，Dao 中会处理
            sequence = 1,
            type = MessageItemType.IMAGE,
            content = filePath,
            createTime = currentTime
        )
        messageDao.createMessageWithDetail(message, item)
    }

    fun selectMessages(): Flow<List<MessageWithContent>> {
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
        messageDao.updateMessageNote(title, id)
    }

    suspend fun updateDetail(detail: String, id: Long) {
        messageDao.updateDetail(detail, id)
    }

    suspend fun deleteMessage(id: Long) {
        messageDao.deleteById(id)
    }

    suspend fun applyNoteAndMerge(note: String) {
        messageDao.applyNoteAndMerge(note)
    }

    fun getLastPhoto(): Flow<String?> {
        return messageDao.getLastPhoto()
    }

    fun selectMessages(query: String): Flow<List<MessageWithContent>> {
        return messageDao.selectMessages(query).flowOn(ioDispatcher)
    }
}
