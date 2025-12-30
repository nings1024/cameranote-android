package com.mnn.cameranote.screens.messagelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(onBack: () -> Unit, onDetailClick: (Long) -> Unit, viewModel: MessageViewModel = koinViewModel()) {
    val messages by viewModel.messages.collectAsStateWithLifecycle(emptyList())
    var query by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }
    var selectedType by rememberSaveable { mutableStateOf("全部") }

    // --- 日期选择相关状态 ---
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var selectedDateText by rememberSaveable { mutableStateOf("") } // 用于展示选中的年月日

    // 格式化函数 (示例: 2023-10-27)
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDateText = dateFormatter.format(Date(millis))
                        // 触发查询：将日期作为过滤条件
//                        viewModel.selectMessage(query, date = selectedDateText)
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }


    Scaffold(
        topBar = {
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // 这是符合最新 Deprecated 提示的新版写法
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (active) 0.dp else 16.dp),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = {
                            active = false
                            viewModel.selectMessage(query)
                        },
                        expanded = active,
                        onExpandedChange = { active = it },
                        placeholder = { Text("搜索消息...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // 1. 日历图标按钮
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                                }
                                // 2. 清除按钮
                                if (query.isNotEmpty() || selectedDateText.isNotEmpty()) {
                                    IconButton(onClick = {
                                        query = ""
                                        selectedDateText = ""
//                                        viewModel.selectMessage("", date = "") // 重置查询
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = null)
                                    }
                                }
                            }
                        }
                    )
                },
                expanded = active,
                onExpandedChange = { active = it }
            ) {
                // 这里是搜索展开后的建议列表（可以留空或写历史记录）
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == "全部",
                        onClick = { selectedType = "全部" },
                        label = { Text("全部") }
                    )
                    FilterChip(
                        selected = selectedType == "已读",
                        onClick = { selectedType = "已读" },
                        label = { Text("已读") }
                    )
                }
            }
            // 如果选中了日期，显示一个可删除的 Chip 提示当前正在过滤日期
            if (selectedDateText.isNotEmpty()) {
                InputChip(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    selected = true,
                    onClick = { /* 点击也可以弹出修改日期 */ },
                    label = { Text(selectedDateText) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            Modifier.size(18.dp).clickable {
                                selectedDateText = ""
//                                viewModel.selectMessage(query, date = "")
                            }
                        )
                    }
                )
            }

            NoteList(
                messages = messages,
                paddingValues = PaddingValues(0.dp), // 因为外层 Column 已经处理了 padding
                onItemClick = onDetailClick,
            )
        }
    }
}
