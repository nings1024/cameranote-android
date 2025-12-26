package com.mnn.cameranote.screens.messagelist

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(onBack: () -> Unit, onDetailClick: (Long) -> Unit, viewModel: MessageViewModel = koinViewModel()) {
    val messages by viewModel.messages.collectAsStateWithLifecycle(emptyList())
    var query by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }
    var selectedType by rememberSaveable { mutableStateOf("全部") }
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
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
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

            NoteList(
                messages = messages,
                paddingValues = PaddingValues(0.dp), // 因为外层 Column 已经处理了 padding
                onItemClick = onDetailClick,
            )
        }
    }
}
