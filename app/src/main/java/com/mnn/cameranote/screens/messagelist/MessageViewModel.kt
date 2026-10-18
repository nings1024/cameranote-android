package com.mnn.cameranote.screens.messagelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnn.cameranote.data.repository.MessageFilter
import com.mnn.cameranote.data.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class MessageViewModel(private val repository: MessageRepository) : ViewModel() {

    private val _filter = MutableStateFlow(MessageFilter())
    val filter: StateFlow<MessageFilter> = _filter.asStateFlow()

    /**
     * 输入时 250ms 防抖，避免每敲一个字就查一次库；
     * 日期/草稿这类离散筛选立即生效（query 为空时 debounce 时长为 0）。
     *
     * 实体 -> UI 模型的转换放在 [Dispatchers.Default] 上，
     * 而不是留在 `stateIn` 的 Main 上做。
     */
    val messages: StateFlow<List<MessageUiModel>> = _filter
        .debounce { filter -> if (filter.query.isBlank()) 0L else SEARCH_DEBOUNCE_MILLIS }
        .distinctUntilChanged()
        .flatMapLatest { filter -> repository.observeMessages(filter) }
        .map { rows -> rows.map { it.toUiModel() } }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun onQueryChange(text: String) {
        _filter.update { it.copy(query = text) }
    }

    fun onDayRangeSelected(dayStart: Long?, dayEnd: Long?) {
        _filter.update { it.copy(dayStart = dayStart, dayEnd = dayEnd, draftOnly = false) }
    }

    fun onDraftOnlySelected(draftOnly: Boolean) {
        _filter.update {
            it.copy(draftOnly = draftOnly, dayStart = null, dayEnd = null)
        }
    }

    fun clearFilters() {
        _filter.value = MessageFilter()
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 250L
    }
}
