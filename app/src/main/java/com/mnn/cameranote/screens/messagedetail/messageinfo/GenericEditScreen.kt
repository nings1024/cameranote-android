package com.mnn.cameranote.screens.messagedetail.messageinfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mnn.cameranote.screens.messagedetail.MessageDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericEditScreen(
    label: String,       // 标题
    hint: String = "",       // 输入框提示词
    onBack: () -> Unit,
    onSave: () -> Unit,
    viewModel: MessageDetailViewModel = koinViewModel()
) {
    val message by viewModel.message.collectAsStateWithLifecycle()
    var isProcessing by remember { mutableStateOf(false) } // 状态拦截位
    val focusManager = LocalFocusManager.current

    // 1. 创建 FocusRequester
    val focusRequester = remember { FocusRequester() }
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                ""
            )
        )
    }
// 监听 message，当它从 null 变成真实数据时，更新 inputText
    LaunchedEffect(message) {
        message.let {
            textFieldValue = textFieldValue.copy(text = it.title, selection = TextRange(0, it.title.length))
        }
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
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
                    // 只有当内容发生变化且不为空时，保存按钮才高亮
                    TextButton(
                        onClick = {
                            viewModel.updateTitle(textFieldValue.text, message.id)
                            onSave()
                        },
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
        Column(modifier = Modifier.padding(padding)) {
            Spacer(modifier = Modifier.height(12.dp))

            // 模仿微信：通栏白色输入区域
            Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                TextField(
                    textFieldValue, { textFieldValue = it },
                    modifier = Modifier.focusRequester(focusRequester).onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            textFieldValue = textFieldValue.copy(
                                selection = TextRange(0, textFieldValue.text.length)
                            )
                        }
                    }.fillMaxWidth(),
                    placeholder = { Text(hint) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done // 将回车键改为“完成”
                    ),
                    // 2. 监听点击“完成”后的事件
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (!isProcessing) {
                                isProcessing = true // 标记开始处理
                                focusManager.clearFocus()
                                viewModel.updateTitle(textFieldValue.text, message.id)
                                onSave()
                            }
                        }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFF07C160), // 微信绿
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        if (textFieldValue.text.isNotEmpty()) {
                            IconButton(onClick = { textFieldValue = textFieldValue.copy("") }) {
                                Icon(Icons.Default.Close, contentDescription = "清空")
                            }
                        }
                    }
                )
            }
        }
    }
}