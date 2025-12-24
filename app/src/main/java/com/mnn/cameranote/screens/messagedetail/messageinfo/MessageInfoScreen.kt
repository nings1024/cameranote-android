package com.mnn.cameranote.screens.messagedetail.messageinfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mnn.cameranote.screens.messagedetail.MessageDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInfoScreen(
    onBack: () -> Unit,
    onUpdateTitle: (Long) -> Unit,
    onUpdateDetail: (Long) -> Unit,
    onDelete: () -> Unit,
    viewModel: MessageDetailViewModel = koinViewModel()
) {

    val message by viewModel.message.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar( // 类似微信的居中标题
                title = { Text("事件", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F5F5),
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                )
            )
        },
        containerColor = Color(0xFFF5F5F5) // 全局背景色
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. 修改主题
            InfoItem(label = "主题", value = message.title) {
                // 这里可以弹出输入框对话框
                onUpdateTitle(message.id)
            }

            // 2. 修改详情
            InfoItem(label = "详情说明", value = message.detailInfo) {
                onUpdateDetail(message.id)
            }

            Spacer(modifier = Modifier.weight(1f)) // 撑开中间空间

            // 3. 删除按钮
            Button(
                onClick = {
                    viewModel.deleteMessage(message.id)
                    onDelete()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White), // 白色背景
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("删除", color = Color.Red, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// 抽离出的条目组件（仿微信样式）
@Composable
fun InfoItem(label: String, value: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, modifier = Modifier.width(100.dp), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = value,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}