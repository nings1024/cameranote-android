package com.mnn.cameranote.screens.messagedetail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mnn.cameranote.data.local.entity.MessageItemEntity
import com.mnn.cameranote.data.local.entity.MessageItemType
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MessageDetailScreen(
    onBack: () -> Unit, viewModel: MessageDetailViewModel = koinViewModel(), onEditClick: (Long) -> Unit
) {
    val context = LocalContext.current
//    事件
    val message by viewModel.message.collectAsStateWithLifecycle()
//    内容列表
    val messageItems by viewModel.messageItems.collectAsStateWithLifecycle()
    // 2. 派生出仅包含图片的列表（通过 derivedStateOf 保证仅在 messageItems 改变时重新计算）
    val imageMessages by remember(messageItems) {
        derivedStateOf {
            messageItems.filter { it.type == MessageItemType.IMAGE }.reversed()
        }
    }

    var selectedImageForPager by remember { mutableStateOf<MessageItemEntity?>(null) }

//    待发送消息
    var inputText by remember { mutableStateOf("") }
//    事件详情
    var isDetailExpanded by remember { mutableStateOf(false) }
//    待发送图片
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }


    // 注册图片选择器 (System Photo Picker)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 9)
    ) { uris->
        selectedImages=selectedImages + uris
    }

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
                selectedImages = selectedImages,
                onRemoveImage = { uri ->
                    selectedImages = selectedImages.filterNot { it == uri }
                },
                onAddImageClick = {
                    // 触发系统选择器
                    launcher.launch(

                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onSendClick = {
                    // 执行上传逻辑
                    viewModel.uploadContent(context,inputText, selectedImages)
                    // 重置状态
                    inputText = ""
                    selectedImages = emptyList()
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding), reverseLayout = true // 聊天常用，新消息在底，列表反转
        ) {
            items(messageItems, key = { it.itemId }) { msg ->
                ChatBubble(
                    message = msg,
                    onImageClick = { clickedMsg ->
                        selectedImageForPager = clickedMsg // 记录当前点击的图片
                    }
                )
            }
        }
        // 统一由 Screen 弹出全屏预览
        selectedImageForPager?.let { clickedMessage ->
            FullScreenImagePager(
                images = imageMessages, // derivedStateOf 产生的那份
                initialIndex = imageMessages.indexOf(clickedMessage).coerceAtLeast(0),
                onDismiss = { selectedImageForPager = null }
            )
        }
    }
}