package com.mnn.cameranote.screens.messagelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnn.cameranote.data.database.repository.MessageRepository
import com.mnn.cameranote.model.MessageItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MessageViewModel(private val repository: MessageRepository): ViewModel() {
    // 使用 stateIn 自动管理数据流的订阅和取消
    val messages: StateFlow<List<MessageItem>> = repository.selectMessages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // 界面不可见 5 秒后停止收集，节省资源
            initialValue = emptyList() // 初始状态为空列表
        )


}