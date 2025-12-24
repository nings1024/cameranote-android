package com.mnn.cameranote.screens.messagedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnn.cameranote.data.database.entity.MessageEntity
import com.mnn.cameranote.data.database.entity.MessageItemEntity
import com.mnn.cameranote.data.database.repository.MessageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MessageDetailModel(private val savedStateHandle: SavedStateHandle, private val repository: MessageRepository) :
    ViewModel() {
    // 直接从 savedStateHandle 中获取 "id"
    private val messageId: Long = checkNotNull(savedStateHandle["id"])
    val message: StateFlow<MessageEntity> = repository.selectMessageById(messageId).stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), // 界面不可见 5 秒后停止收集，节省资源
        initialValue = MessageEntity(1, "加载中", 3, "未知", "加载中")
    )
    val messageItems: StateFlow<List<MessageItemEntity>> = repository.selectMessageItemById(messageId).stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), // 界面不可见 5 秒后停止收集，节省资源
        initialValue = emptyList()
    )
}