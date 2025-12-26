package com.mnn.cameranote.database.dao

import android.content.ContentValues.TAG
import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.mnn.cameranote.database.entity.MessageEntity
import com.mnn.cameranote.database.entity.MessageItemEntity
import com.mnn.cameranote.database.entity.MessageItemSource
import com.mnn.cameranote.database.entity.MessageItemType
import com.mnn.cameranote.database.entity.relations.MessageWithContent
import kotlinx.coroutines.flow.Flow

// data/database/dao/MessageDao.kt
@Dao
interface MessageDao {
    @Insert
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert
    suspend fun insertMessageItem(item: MessageItemEntity): Long

    @Transaction
    suspend fun createMessageWithDetail(message: MessageEntity, item: MessageItemEntity) {
        val id = insertMessage(message)
        // 将生成的主表 ID 赋值给子表的外键
        insertMessageItem(item.copy(messageId = id))
    }

    // 查询：所有消息（LiveData版本）
    @Query("SELECT * FROM message_items")
    fun getAllMessages(): Flow<List<MessageItemEntity>>

    @Query(
        """
            SELECT 
        a.*,
        b.content 
    FROM 
        messages a
    LEFT JOIN message_items b ON b.itemId = (
        SELECT itemId 
        FROM message_items b2 
        WHERE b2.messageId = a.id 
        and b2.type=:type
        and b2.source=:source
        ORDER BY b2.sequence ASC, b2.itemId ASC 
        LIMIT 1
    )
    where a.isDeleted = 0
    order by a.createTime desc
    """
    )
    fun selectMessages(
        type: Int = MessageItemType.IMAGE.value, source: Int = MessageItemSource.CAMERA.value
    ): Flow<List<MessageWithContent>>

    @Query("SELECT * FROM messages WHERE id = :id")
    fun selectMessageById(id: Long): Flow<MessageEntity>

    @Query("SELECT * FROM message_items WHERE messageId = :id order by createTime desc")
    fun selectMessageItemById(id: Long): Flow<List<MessageItemEntity>>

    @Query("UPDATE messages SET title = :title,isTemp=0 WHERE id = :id")
    suspend fun updateMessageNote(title: String, id: Long)

    @Query("UPDATE messages SET detailInfo = :detail WHERE id = :id")
    suspend fun updateDetail(detail: String, id: Long)


    @Query("update messages set isDeleted=1 WHERE id = :messageId")
    suspend fun deleteById(messageId: Long)


    @Query("SELECT id FROM messages WHERE isTemp = 1 AND createTime > :timeLimit order by createTime desc")
    suspend fun getUnnotedMessageIds(timeLimit: Long): List<Long>

    // Repository
    suspend fun applyNoteAndMerge(note: String) {
        val timeLimit = System.currentTimeMillis() - 5 * 60 * 1000

        // 1. 找到该 ID 之前 5 分钟内所有临时的、没备注的消息 ID
        val idsToMerge = getUnnotedMessageIds(timeLimit)
        Log.d(TAG, "applyNoteAndMerge: $idsToMerge")
        if (idsToMerge.isEmpty()) {
            return
        }
        val firstId = idsToMerge.first()
        val otherIds = idsToMerge.drop(1)
        updateMessageNote(note, firstId)
        Log.d(TAG, "applyNoteAndMerge: $otherIds")
        if (otherIds.isNotEmpty()) {
            Log.d(TAG, "applyNoteAndMerge: $otherIds $firstId")
            moveItemsToMessage(fromIds = otherIds, toId = firstId)
            deleteMessages(otherIds)
        }
    }

    @Query("DELETE FROM messages WHERE id IN (:ids)")
    suspend fun deleteMessages(ids: List<Long>)

    @Query("UPDATE message_items set messageId=:toId WHERE messageId IN (:fromIds)")
    suspend fun moveItemsToMessage(fromIds: List<Long>, toId: Long)

    @Query(
        """
        SELECT content FROM message_items where isDeleted=0 and type=:type order by createTime desc limit 1
    """
    )
    fun getLastPhoto(type: MessageItemType = MessageItemType.IMAGE): Flow<String?>
}