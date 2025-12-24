package com.mnn.cameranote.screens.messagedetail

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MessageDetailScreen(
    onBack: () -> Unit, viewModel: MessageDetailViewModel = koinViewModel(), onEditClick: (Long) -> Unit
) {
    // 从 ViewModel 获取状态
    val message by viewModel.message.collectAsStateWithLifecycle()
    val messageItems by viewModel.messageItems.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    var isDetailExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .animateContentSize() // 让展开和折叠更平滑
                    .clickable { isDetailExpanded = !isDetailExpanded }
            ) {
                // 1. 标题行
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左侧：返回按钮
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }

                    // 中间：标题 (占据剩余所有空间)
                    Text(
                        text = message.title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )

                    // 右侧：设置按钮 (新增)
                    IconButton(onClick = { onEditClick(message.id) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置"
                        )
                    }
                }

                // 2. 详情预览/全文区
                Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                    Text(
                        text = message.detailInfo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        // 如果不是展开状态，就限制 1 行并显示省略号
                        maxLines = if (isDetailExpanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        bottomBar = {
            // 底部输入区域
            ChatInputBar(
                text = inputText,
                onTextChange = { inputText = it },
                onSendText = {
                    Log.d(TAG, "MessageDetailScreen: $inputText")
                    viewModel.sendMessage(inputText)
                    inputText = ""
                },
                onAddImage = { /* 调用系统相册选择器 */ }
            )
        }
    ) { innerPadding ->
        // 中间消息列表
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding), reverseLayout = true // 聊天常用，新消息在底，列表反转
        ) {
            items(messageItems) { msg ->
                ChatBubble(msg)
            }
        }
    }
}