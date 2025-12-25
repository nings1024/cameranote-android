package com.mnn.cameranote.screens.messagedetail

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnn.cameranote.database.entity.MessageEntity
import com.mnn.cameranote.database.entity.MessageItemEntity
import com.mnn.cameranote.database.entity.MessageItemSource
import com.mnn.cameranote.database.entity.MessageItemType
import com.mnn.cameranote.database.repository.MessageRepository
import com.mnn.cameranote.util.createYearMonthDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MessageDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: MessageRepository
) :
    ViewModel() {
    fun sendMessage(inputText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertOneMessage(
                MessageItemEntity(
                    messageId = messageId,
                    content = inputText,
                    sequence = 1,
                    type = MessageItemType.TEXT,
                    createTime = System.currentTimeMillis()
                )
            )
        }
    }

    // 1. 编写一个辅助函数：将 Uri 转换为你的私有 File
    fun copyUriToPrivateStorage(context: Context, uri: Uri): File? {
        val storageDir = context.createYearMonthDirectory()
        val fileName = "IMG_${System.currentTimeMillis()}_${(100..999).random()}.jpg"
        val destFile = File(storageDir, fileName)

        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                destFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream) // 核心操作：流拷贝
                }
            }
            destFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun uploadContent(context: Context, inputText: String, selectedImages: List<Uri>) {
        viewModelScope.launch {
            // 1. 先处理文本消息
            if (inputText.isNotBlank()) {
                sendMessage(inputText)
            }

            // 2. 处理图片（在 IO 线程进行）
            if (selectedImages.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    selectedImages.forEach { uri ->
                        val savedFile = copyUriToPrivateStorage(context, uri)
                        if (savedFile != null) {
                            // 插入图片消息（复用你之前的逻辑）
                            repository.insertOneMessage(
                                MessageItemEntity(
                                    messageId = messageId, // 确保这个 ID 是正确生成的逻辑
                                    content = savedFile.absolutePath, // 存入路径
                                    sequence = 1,
                                    type = MessageItemType.IMAGE,
                                    source = MessageItemSource.UPLOAD
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // 直接从 savedStateHandle 中获取 "id"
    private val messageId: Long = checkNotNull(savedStateHandle["id"])

    fun updateTitle(title: String, id: Long) {
        viewModelScope.launch {
            repository.updateMessage(title, id)
        }
    }

    fun updateDetail(title: String, id: Long) {
        viewModelScope.launch {
            repository.updateDetail(title, id)
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }


    val message: StateFlow<MessageEntity> = repository.selectMessageById(messageId).stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), // 界面不可见 5 秒后停止收集，节省资源
        initialValue = MessageEntity(
            title = "加载中",
            location = "未知",
            detailInfo = "加载中"
        )
    )
    val messageItems: StateFlow<List<MessageItemEntity>> = repository.selectMessageItemById(messageId).stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), // 界面不可见 5 秒后停止收集，节省资源
        initialValue = emptyList()
    )
}