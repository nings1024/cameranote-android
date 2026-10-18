package com.mnn.cameranote.screens.messagelist

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mnn.cameranote.util.formatDayLabel
import com.mnn.cameranote.util.todayRange
import com.mnn.cameranote.util.utcDateMillisToLocalDayRange
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    onBack: () -> Unit,
    onDetailClick: (Long) -> Unit,
    viewModel: MessageViewModel = koinViewModel(),
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val today = remember { todayRange() }
    val isAllSelected = filter.dayStart == null && filter.dayEnd == null && !filter.draftOnly
    val isTodaySelected = filter.dayStart == today.first
    val hasCustomDate = filter.dayStart != null && !isTodaySelected
    val hasAnyFilter = filter.query.isNotBlank() || filter.dayStart != null || filter.draftOnly

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // DatePicker 给的是 UTC 当天 0 点，换算成设备时区的当天区间
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            val (start, end) = utcDateMillisToLocalDayRange(utcMillis)
                            viewModel.onDayRangeSelected(start, end)
                        }
                        showDatePicker = false
                    },
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("备忘") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            SearchField(
                query = filter.query,
                onQueryChange = viewModel::onQueryChange,
                onPickDate = { showDatePicker = true },
            )

            FilterRow(
                isAllSelected = isAllSelected,
                isTodaySelected = isTodaySelected,
                isDraftSelected = filter.draftOnly,
                customDateLabel = filter.dayStart?.takeIf { hasCustomDate }?.let { formatDayLabel(it) },
                onSelectAll = viewModel::clearFilters,
                onSelectToday = { viewModel.onDayRangeSelected(today.first, today.second) },
                onSelectDraft = { viewModel.onDraftOnlySelected(true) },
                onClearDate = { viewModel.onDayRangeSelected(null, null) },
            )

            if (messages.isEmpty()) {
                EmptyState(hasAnyFilter = hasAnyFilter)
            } else {
                NoteList(
                    messages = messages,
                    onItemClick = onDetailClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onPickDate: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("搜索标题、描述或内容") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPickDate) {
                        Icon(Icons.Default.DateRange, contentDescription = "按日期筛选")
                    }
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "清空搜索")
                        }
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun FilterRow(
    isAllSelected: Boolean,
    isTodaySelected: Boolean,
    isDraftSelected: Boolean,
    customDateLabel: String?,
    onSelectAll: () -> Unit,
    onSelectToday: () -> Unit,
    onSelectDraft: () -> Unit,
    onClearDate: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(selected = isAllSelected, onClick = onSelectAll, label = { Text("全部") })
        FilterChip(selected = isTodaySelected, onClick = onSelectToday, label = { Text("今天") })
        FilterChip(selected = isDraftSelected, onClick = onSelectDraft, label = { Text("待备注") })
        if (customDateLabel != null) {
            InputChip(
                selected = true,
                onClick = onClearDate,
                label = { Text(customDateLabel) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "清除日期筛选",
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun EmptyState(hasAnyFilter: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hasAnyFilter) {
                    "没有符合条件的备忘"
                } else {
                    "还没有备忘\n回到相机，拍下第一件事"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
