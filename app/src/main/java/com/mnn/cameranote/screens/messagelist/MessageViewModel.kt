package com.mnn.cameranote.screens.messagelist

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnn.cameranote.database.repository.MessageRepository
import kotlinx.coroutines.flow.*

class MessageViewModel(private val repository: MessageRepository) : ViewModel() {


    // 1. 定义一个私有的状态流，专门存当前的搜索词
    // 初始值设为空字符串 ""
    private val searchQuery = MutableStateFlow("")

    // 2. 将搜索词流转换成数据流
    val messages: StateFlow<List<MessageUiModel>> = searchQuery
        .flatMapLatest { query ->
            // 每当 searchQuery 变化，这里会自动重新调用 repository
            if (query.isBlank()) {
                // 如果搜索词为空，你可以选择返回全部或者空
                repository.selectMessages()
            } else {
                repository.selectMessages(query)
            }
        }
        .map { entities ->
            entities.map { it.toUiModel() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 3. 现在的搜索方法变得非常简单：只是更新搜索词
    fun selectMessage(query: String) {
        Log.d(TAG, "selectMessage: $query")
        // 只需更新这个 stateFlow，上面的 flatMapLatest 就会自动触发
        searchQuery.value = query
    }
}