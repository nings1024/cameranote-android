package com.mnn.cameranote.screens.messagedetail

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mnn.cameranote.data.local.entity.MessageEntity
import com.mnn.cameranote.data.local.entity.MessageItemEntity
import com.mnn.cameranote.data.local.entity.MessageItemSource
import com.mnn.cameranote.data.local.entity.MessageItemType
import com.mnn.cameranote.data.manager.FileManager
import com.mnn.cameranote.data.repository.MessageRepository
import com.mnn.cameranote.navigation.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MessageDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: MessageRepository,
    private val fileManager: FileManager
) :
    ViewModel() {

    // 状态安全地提取 ID (配合我们之前的类型安全重构)
    private val route = savedStateHandle.toRoute<Route.Detail>()
    private val messageId = route.id

    // 增加一个 UI 状态来控制按钮是否可用
    private val _isSending = MutableStateFlow(false)
    val isSending = _isSending.asStateFlow()


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

    fun uploadContent(context: Context, inputText: String, selectedImages: List<Uri>) {
        if (_isSending.value) return // 防止重复点击
        viewModelScope.launch {
            _isSending.value = true
            // 1. 先处理文本消息
            if (inputText.isNotBlank()) {
                sendMessage(inputText)
            }
            // 2. 并行处理图片 (高级技巧：async + awaitAll)
            if (selectedImages.isNotEmpty()) {
                selectedImages.map { uri ->
                    async(Dispatchers.IO) {
                        val path = fileManager.saveUriToPrivate(uri)
                        if (path != null) {
                            repository.insertOneMessage(
                                MessageItemEntity(
                                    messageId = messageId, // 确保这个 ID 是正确生成的逻辑
                                    content = path, // 存入路径
                                    sequence = 1,
                                    type = MessageItemType.IMAGE,
                                    source = MessageItemSource.UPLOAD
                                )
                            )
                        }
                    }
                }.awaitAll() // 等待所有图片处理完成
            }
            _isSending.value = false
        }
    }


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