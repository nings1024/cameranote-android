package com.mnn.cameranote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mnn.cameranote.data.local.entity.MessageEntity
import com.mnn.cameranote.data.local.entity.MessageItemEntity
import com.mnn.cameranote.data.local.entity.MessageItemType
import com.mnn.cameranote.data.local.entity.relations.MessageWithContent
import kotlinx.coroutines.flow.Flow

/**
 * 纯数据访问。业务逻辑（草稿合并、文件清理等）放在 MessageRepository。
 */
@Dao
interface MessageDao {

    // ---------------------------------------------------------------- 写入

    @Insert
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert
    suspend fun insertMessageItem(item: MessageItemEntity): Long

    /** 写入标题并归档（草稿 -> 正式备忘）。 */
    @Query("UPDATE messages SET title = :title, isTemp = 0 WHERE id = :id")
    suspend fun updateTitle(title: String, id: Long)

    @Query("UPDATE messages SET detailInfo = :detail WHERE id = :id")
    suspend fun updateDetail(detail: String, id: Long)

    @Query("UPDATE messages SET isDeleted = 1, deletedTime = :deletedTime WHERE id = :id")
    suspend fun softDeleteMessage(id: Long, deletedTime: Long)

    @Query("UPDATE messages SET isDeleted = 1, deletedTime = :deletedTime WHERE id IN (:ids)")
    suspend fun softDeleteMessages(ids: List<Long>, deletedTime: Long)

    @Query("UPDATE message_items SET isDeleted = 1, deletedTime = :deletedTime WHERE messageId = :messageId")
    suspend fun softDeleteItemsOf(messageId: Long, deletedTime: Long)

    @Query("UPDATE message_items SET messageId = :toId WHERE messageId IN (:fromIds)")
    suspend fun moveItemsToMessage(fromIds: List<Long>, toId: Long)

    // ---------------------------------------------------------------- 草稿

    /** 当前正在收集的草稿（最多只应有一个）。 */
    @Query(
        """
        SELECT * FROM messages
        WHERE isDeleted = 0 AND isTemp = 1
        ORDER BY createTime DESC, id DESC
        LIMIT 1
        """
    )
    suspend fun findOpenDraft(): MessageEntity?

    /** 历史遗留可能同时存在多个草稿，这里全部取出用于一次性合并。 */
    @Query(
        """
        SELECT id FROM messages
        WHERE isDeleted = 0 AND isTemp = 1
        ORDER BY createTime DESC, id DESC
        """
    )
    suspend fun findDraftIds(): List<Long>

    @Query("SELECT COALESCE(MAX(sequence), 0) FROM message_items WHERE messageId = :messageId")
    suspend fun maxSequence(messageId: Long): Int

    // ---------------------------------------------------------------- 查询

    @Query("SELECT * FROM messages WHERE id = :id AND isDeleted = 0")
    fun observeMessage(id: Long): Flow<MessageEntity?>

    /** 条目按 [MessageItemEntity.itemId] 倒序（最新在前，配合 reverseLayout 使用）。 */
    @Query(
        """
        SELECT * FROM message_items
        WHERE messageId = :id AND isDeleted = 0
        ORDER BY itemId DESC
        """
    )
    fun observeItems(id: Long): Flow<List<MessageItemEntity>>

    /**
     * 相机页左下角的「最近一张照片」。必须连表排除已删除的备忘，
     * 否则删掉备忘后缩略图还会显示那张照片。
     */
    @Query(
        """
        SELECT i.content
        FROM message_items i
        INNER JOIN messages m ON m.id = i.messageId
        WHERE i.isDeleted = 0
          AND m.isDeleted = 0
          AND i.type = :type
        ORDER BY i.createTime DESC, i.itemId DESC
        LIMIT 1
        """
    )
    fun observeLastPhoto(type: Int = MessageItemType.IMAGE.value): Flow<String?>

    /** 当前草稿已收录的条目数，用于相机页提示。 */
    @Query(
        """
        SELECT COUNT(*)
        FROM message_items i
        INNER JOIN messages m ON m.id = i.messageId
        WHERE m.isTemp = 1 AND m.isDeleted = 0 AND i.isDeleted = 0
        """
    )
    fun observeDraftItemCount(): Flow<Int>

    /** 删除备忘前取出所有图片路径，用于同步清理磁盘文件。 */
    @Query(
        """
        SELECT content FROM message_items
        WHERE messageId = :messageId AND type = :type AND isDeleted = 0
        """
    )
    suspend fun imagePathsOf(
        messageId: Long,
        type: Int = MessageItemType.IMAGE.value,
    ): List<String>

    /**
     * 列表页主查询：一次取回封面与条目数，避免 N+1。
     *
     * @param rawQuery 已由 Repository 转义过的搜索词（null / 空表示不过滤）
     */
    @Query(
        """
        SELECT
            a.*,
            (
                SELECT i.content FROM message_items i
                WHERE i.messageId = a.id
                  AND i.type = :imageType
                  AND i.isDeleted = 0
                ORDER BY i.itemId ASC
                LIMIT 1
            ) AS content,
            (
                SELECT COUNT(*) FROM message_items c
                WHERE c.messageId = a.id AND c.isDeleted = 0
            ) AS itemCount
        FROM messages a
        WHERE a.isDeleted = 0
          AND (:dayStart IS NULL OR a.createTime >= :dayStart)
          AND (:dayEnd IS NULL OR a.createTime < :dayEnd)
          AND (:draftOnly = 0 OR a.isTemp = 1)
          AND (
                :rawQuery IS NULL
                OR a.title LIKE '%' || :rawQuery || '%' ESCAPE '\'
                OR a.detailInfo LIKE '%' || :rawQuery || '%' ESCAPE '\'
                OR EXISTS (
                    SELECT 1 FROM message_items s
                    WHERE s.messageId = a.id
                      AND s.isDeleted = 0
                      AND s.type = :textType
                      AND s.content LIKE '%' || :rawQuery || '%' ESCAPE '\'
                )
              )
        ORDER BY a.createTime DESC, a.id DESC
        """
    )
    fun observeMessages(
        rawQuery: String?,
        dayStart: Long?,
        dayEnd: Long?,
        draftOnly: Boolean,
        imageType: Int = MessageItemType.IMAGE.value,
        textType: Int = MessageItemType.TEXT.value,
    ): Flow<List<MessageWithContent>>
}
