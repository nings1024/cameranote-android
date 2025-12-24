package com.mnn.cameranote.screens.messagedetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MessageDetailScreen(
    onBack: () -> Unit, viewModel: MessageDetailModel = koinViewModel()
) {
    // 从 ViewModel 获取状态
    val message by viewModel.message.collectAsStateWithLifecycle()
    val messageItems by viewModel.messageItems.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    var isDetailExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // 自定义顶部栏：标题 + 详情
            Column(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).statusBarsPadding()
            ) {
                // 1. 标题行
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                    Text(
                        text = message.title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    // 详情开关按钮
                    TextButton(onClick = { isDetailExpanded = !isDetailExpanded }) {
                        Text(if (isDetailExpanded) "收起详情" else "查看详情")
                    }
                }

                // 2. 可折叠详情区
                AnimatedVisibility(visible = isDetailExpanded) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text(
                            message.detailInfo, style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }) { innerPadding ->
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