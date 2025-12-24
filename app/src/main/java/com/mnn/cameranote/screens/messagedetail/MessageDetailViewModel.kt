package com.mnn.cameranote.screens.messagedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnn.cameranote.database.entity.MessageEntity
import com.mnn.cameranote.database.entity.MessageItemEntity
import com.mnn.cameranote.database.entity.MessageType
import com.mnn.cameranote.database.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
                    type = MessageType.TEXT.ordinal,
                    createTime = System.currentTimeMillis()
                )
            )
        }
    }

    // 直接从 savedStateHandle 中获取 "id"
    private val messageId: Long = checkNotNull(savedStateHandle["id"])

    fun updateTitle(title: String, id: Long) {
        viewModelScope.launch {
            repository.updateMessage(title,id)
        }
    }
    fun updateDetail(title: String, id: Long) {
        viewModelScope.launch {
            repository.updateDetail(title,id)
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }


    val message: StateFlow<MessageEntity> = repository.selectMessageById(messageId).stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), // 界面不可见 5 秒后停止收集，节省资源
        initialValue = MessageEntity(1, "加载中", 3, "未知", "加载中")
    )
    val messageItems: StateFlow<List<MessageItemEntity>> = repository.selectMessageItemById(messageId).stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), // 界面不可见 5 秒后停止收集，节省资源
        initialValue = emptyList()
    )
}