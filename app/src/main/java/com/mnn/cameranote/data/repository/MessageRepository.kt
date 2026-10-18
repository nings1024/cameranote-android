package com.mnn.cameranote.data.repository

import android.net.Uri
import androidx.room.withTransaction
import com.mnn.cameranote.data.local.AppDatabase
import com.mnn.cameranote.data.local.dao.MessageDao
import com.mnn.cameranote.data.local.entity.MessageEntity
import com.mnn.cameranote.data.local.entity.MessageItemEntity
import com.mnn.cameranote.data.local.entity.MessageItemSource
import com.mnn.cameranote.data.local.entity.MessageItemType
import com.mnn.cameranote.data.local.entity.relations.MessageWithContent
import com.mnn.cameranote.data.manager.FileStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime

/** 列表页筛选条件。 */
data class MessageFilter(
    val query: String = "",
    val dayStart: Long? = null,
    val dayEnd: Long? = null,
    /** 只看还没补描述的草稿 */
    val draftOnly: Boolean = false,
) {
    /** 转义 LIKE 通配符，避免用户输入 `%` / `_` 时把整张表搜出来。 */
    fun escapedQueryOrNull(): String? {
        val q = query.trim()
        if (q.isEmpty()) return null
        return q
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
    }
}

/**
 * 备忘仓库：所有「跨表 + 跨存储」的操作都在这里，且都在事务里完成。
 *
 * 核心理念：拍照优先。拍照只会往**当前草稿**里追加条目；用户补完描述后
 * 草稿才归档成一个正式备忘。所以不存在的「多草稿合并」是为了兼容历史数据。
 */
class MessageRepository(
    private val messageDao: MessageDao,
    private val database: AppDatabase,
    private val fileStore: FileStore,
    private val ioDispatcher: CoroutineDispatcher,
) {

    // ------------------------------------------------------------ 读

    fun observeMessages(filter: MessageFilter): Flow<List<MessageWithContent>> =
        messageDao.observeMessages(
            rawQuery = filter.escapedQueryOrNull(),
            dayStart = filter.dayStart,
            dayEnd = filter.dayEnd,
            draftOnly = filter.draftOnly,
        ).flowOn(ioDispatcher)

    fun observeMessage(id: Long): Flow<MessageEntity?> =
        messageDao.observeMessage(id).flowOn(ioDispatcher)

    fun observeItems(messageId: Long): Flow<List<MessageItemEntity>> =
        messageDao.observeItems(messageId).flowOn(ioDispatcher)

    fun observeLastPhoto(): Flow<File?> =
        messageDao.observeLastPhoto()
            .map { path -> path?.let(::File) }
            .flowOn(ioDispatcher)

    fun observeDraftItemCount(): Flow<Int> =
        messageDao.observeDraftItemCount().flowOn(ioDispatcher)

    // ------------------------------------------------------------ 写

    /** 拍照落盘后调用：追加到当前草稿，没有草稿就新建一条。 */
    suspend fun addCapturedPhoto(path: String) = withContext(ioDispatcher) {
        database.withTransaction {
            val draftId = messageDao.findOpenDraft()?.id ?: createDraft()
            appendItem(draftId, path, MessageItemType.IMAGE, MessageItemSource.CAMERA)
        }
    }

    /**
     * 往一条**已存在**的备忘里补拍一张照片（详情页的「拍一张」）。
     *
     * 与 [addCapturedPhoto] 的区别是不碰草稿逻辑：直接追加到指定的备忘上。
     */
    suspend fun appendPhotoToMessage(messageId: Long, path: String) = withContext(ioDispatcher) {
        database.withTransaction {
            appendItem(messageId, path, MessageItemType.IMAGE, MessageItemSource.CAMERA)
        }
    }

    /**
     * 补描述并归档。
     *
     * 历史数据里可能同时存在多个草稿（旧版本「每张照片一条记录」的遗留），
     * 这里一次性把它们合并到最新的一条上，整个过程在事务内，不会丢照片。
     *
     * @return 是否真的归档了内容
     */
    suspend fun saveNote(note: String): Boolean = withContext(ioDispatcher) {
        database.withTransaction {
            val draftIds = messageDao.findDraftIds()
            if (draftIds.isEmpty()) return@withTransaction false

            val keeperId = draftIds.first()
            val redundantIds = draftIds.drop(1)
            messageDao.updateTitle(note, keeperId)
            if (redundantIds.isNotEmpty()) {
                messageDao.moveItemsToMessage(redundantIds, keeperId)
                messageDao.softDeleteMessages(redundantIds, System.currentTimeMillis())
            }
            true
        }
    }

    suspend fun updateTitle(title: String, id: Long) = withContext(ioDispatcher) {
        messageDao.updateTitle(title, id)
    }

    suspend fun updateDetail(detail: String, id: Long) = withContext(ioDispatcher) {
        messageDao.updateDetail(detail, id)
    }

    /** 追加一条文字（备注候补补充）。 */
    suspend fun appendText(messageId: Long, text: String) = withContext(ioDispatcher) {
        database.withTransaction {
            appendItem(messageId, text, MessageItemType.TEXT, MessageItemSource.CAMERA)
        }
    }

    /**
     * 追加从相册选中的图片。
     *
     * 先并行拷贝文件，再逐条入库（顺序即 [MessageItemEntity.itemId] 顺序）。
     * @return 成功入库的图片数量
     */
    suspend fun appendUploadedImages(messageId: Long, uris: List<Uri>): Int =
        withContext(ioDispatcher) {
            if (uris.isEmpty()) return@withContext 0

            val paths = coroutineScope {
                uris.map { uri -> async { fileStore.saveUriToPrivate(uri) } }.awaitAll()
            }.filterNotNull()

            if (paths.isEmpty()) return@withContext 0

            try {
                database.withTransaction {
                    paths.forEach { path ->
                        appendItem(messageId, path, MessageItemType.IMAGE, MessageItemSource.UPLOAD)
                    }
                }
            } catch (t: Throwable) {
                // 入团结转失败就把已经拷进来的文件清掉，避免留下孤儿文件
                paths.forEach { fileStore.deleteQuietly(it) }
                throw t
            }
            paths.size
        }

    /**
     * 软删除备忘：同时标记子项、清理磁盘文件。
     *
     * 文件删除放在事务之后，避免事务回滚时文件已经被删掉。
     */
    suspend fun deleteMessage(id: Long) = withContext(ioDispatcher) {
        val paths = database.withTransaction {
            val now = System.currentTimeMillis()
            val imagePaths = messageDao.imagePathsOf(id)
            messageDao.softDeleteItemsOf(id, now)
            messageDao.softDeleteMessage(id, now)
            imagePaths
        }
        paths.forEach { fileStore.deleteQuietly(it) }
    }

    // ------------------------------------------------------------ 内部

    private suspend fun createDraft(): Long {
        val now = LocalDateTime.now()
        val timestamp = System.currentTimeMillis()
        return messageDao.insertMessage(
            MessageEntity(
                title = now.toLocalDate().toString(),
                location = "",
                detailInfo = "",
                isTemp = true,
                year = now.year,
                month = now.monthValue,
                day = now.dayOfMonth,
                createTime = timestamp,
            ),
        )
    }

    private suspend fun appendItem(
        messageId: Long,
        content: String,
        type: MessageItemType,
        source: MessageItemSource,
    ) {
        val sequence = messageDao.maxSequence(messageId) + 1
        messageDao.insertMessageItem(
            MessageItemEntity(
                messageId = messageId,
                sequence = sequence,
                type = type,
                content = content,
                source = source,
                createTime = System.currentTimeMillis(),
            ),
        )
    }
}
