package com.mnn.cameranote.screens.messagedetail.messageinfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mnn.cameranote.screens.messagedetail.MessageDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericDetailEditScreen(
    label: String = "详细描述",
    hint: String = "请输入详细内容...",
    onBack: () -> Unit,
    onSave: () -> Unit,
    viewModel: MessageDetailViewModel = koinViewModel()
) {
    val message by viewModel.message.collectAsStateWithLifecycle()
    // 使用 remember(message?.id) 这种方式比 LaunchedEffect 更稳，能防止 ID 变化时状态不重置
    var inputText by remember { mutableStateOf("") }
    // 监听 message，当它从 null 变成真实数据时，更新 inputText
    LaunchedEffect(message) {
        message.let {
            inputText = it.detailInfo
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(label, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // 只有 message 不为空才保存
                            message.let {
                                viewModel.updateDetail(inputText, it.id)
                                onSave()
                            }
                        },
                        // 如果内容没变，可以考虑置灰（可选逻辑）
                    ) {
                        Text(
                            "完成",
                            color = Color(0xFF07C160),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 微信长文本风格：全屏或大面积白色区域
            Surface(
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // 让输入区域占据剩余空间
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.fillMaxSize(),
                    placeholder = { Text(hint) },
                    // --- 关键配置：长文本必备 ---
                    singleLine = false, // 允许换行
                    minLines = 5,       // 最小显示行数
                    // ------------------------
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent, // 长文本通常不需要底部的线
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Default // 确保回车键是“换行”
                    )
                )
            }
        }
    }
}