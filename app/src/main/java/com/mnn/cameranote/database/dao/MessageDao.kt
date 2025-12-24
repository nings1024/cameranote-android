package com.mnn.cameranote.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.mnn.cameranote.data.database.entity.MessageEntity
import com.mnn.cameranote.data.database.entity.MessageItemEntity
import com.mnn.cameranote.model.MessageItem
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

    @Query("""
            SELECT 
        a.*,
        b.content
    FROM 
        messages a
    LEFT JOIN message_items b ON b.itemId = (
        SELECT id 
        FROM message_items b2 
        WHERE b2.messageId = a.id 
        and b2.type=1
        ORDER BY b2.sequence ASC, b2.itemId ASC 
        LIMIT 1
    )
    order by a.createTime desc
    """)
    fun selectMessages():Flow<List<MessageItem>>

    @Query("SELECT * FROM messages WHERE id = :id")
    fun selectMessageById(id: Long):Flow<MessageEntity>

    @Query("SELECT * FROM message_items WHERE messageId = :id order by createTime desc")
    fun selectMessageItemById(id: Long):Flow<List<MessageItemEntity>>

    @Query("UPDATE messages SET title = :title WHERE id = :id")
    suspend fun updateMessage(title: String, id: Long)

}