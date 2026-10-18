package com.mnn.cameranote.screens.messagedetail

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mnn.cameranote.data.local.entity.MessageItemEntity
import com.mnn.cameranote.data.local.entity.MessageItemType
import com.mnn.cameranote.data.manager.GallerySaver
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MessageDetailScreen(
    onBack: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: MessageDetailViewModel = koinViewModel(),
) {
    val message by viewModel.message.collectAsStateWithLifecycle()
    val messageItems by viewModel.messageItems.collectAsStateWithLifecycle()
    val input by viewModel.input.collectAsStateWithLifecycle()
    val selectedImages by viewModel.selectedImages.collectAsStateWithLifecycle()
    val isSending by viewModel.isSending.collectAsStateWithLifecycle()
    val isCapturing by viewModel.isCapturing.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var isDetailExpanded by rememberSaveable { mutableStateOf(false) }
    var previewItem by remember { mutableStateOf<MessageItemEntity?>(null) }
    var showAttachSheet by remember { mutableStateOf(false) }
    var showQuickCapture by remember { mutableStateOf(false) }

    // 与列表的视觉顺序一致（旧 -> 新），左右翻页方向才符合直觉
    val imageItems = remember(messageItems) {
        messageItems.asReversed().filter { it.type == MessageItemType.IMAGE }
    }

    val pickImages = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 9),
    ) { uris ->
        viewModel.onImagesPicked(uris)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DetailEvent.Message -> snackbarHostState.showSnackbar(event.text)
                DetailEvent.PhotoAdded -> {
                    showQuickCapture = false
                    snackbarHostState.showSnackbar("已添加到这条备忘")
                }
            }
        }
    }

    // Android 10 以下写入系统相册需要存储权限
    val needsLegacyStoragePermission = remember { GallerySaver.requiresLegacyPermission() }
    val storagePermission = if (needsLegacyStoragePermission) {
        rememberPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    } else {
        null
    }
    val storageGranted = storagePermission?.status?.isGranted ?: true
    var pendingSaveItem by remember { mutableStateOf<MessageItemEntity?>(null) }

    val requestSaveToGallery: (MessageItemEntity) -> Unit = { item ->
        val permission = storagePermission
        if (permission != null && !permission.status.isGranted) {
            pendingSaveItem = item
            permission.launchPermissionRequest()
        } else {
            viewModel.saveToGallery(item)
        }
    }

    LaunchedEffect(storageGranted) {
        if (storageGranted) {
            pendingSaveItem?.let { viewModel.saveToGallery(it) }
            pendingSaveItem = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .animateContentSize(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }

                    Text(
                        text = message?.title ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    IconButton(
                        onClick = { message?.let { onEditClick(it.id) } },
                        enabled = message != null,
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "备忘设置")
                    }
                }

                message?.detailInfo?.takeIf { it.isNotBlank() }?.let { detail ->
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (isDetailExpanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDetailExpanded = !isDetailExpanded }
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    )
                }
            }
        },
        bottomBar = {
            ChatInputBar(
                text = input,
                onTextChange = viewModel::onInputChange,
                selectedImages = selectedImages,
                onRemoveImage = viewModel::onImageRemoved,
                onAddAttachmentClick = { showAttachSheet = true },
                onSendClick = viewModel::submit,
                isSending = isSending,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        if (messageItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                    Text(
                        text = "这条备忘还没有内容\n可以在下面补充文字或图片",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(items = messageItems, key = { it.itemId }) { item ->
                    ChatBubble(
                        message = item,
                        onImageClick = { previewItem = it },
                        onImageLongClick = { requestSaveToGallery(it) },
                    )
                }
            }
        }
    }

    previewItem?.let { clicked ->
        FullScreenImagePager(
            images = imageItems,
            initialIndex = imageItems.indexOf(clicked).coerceAtLeast(0),
            onDismiss = { previewItem = null },
            onSaveClick = { requestSaveToGallery(it) },
        )
    }

    if (showAttachSheet) {
        AttachActionSheet(
            onDismiss = { showAttachSheet = false },
            onTakePhoto = {
                showAttachSheet = false
                showQuickCapture = true
            },
            onPickImages = {
                showAttachSheet = false
                pickImages.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
        )
    }

    if (showQuickCapture) {
        QuickCaptureDialog(
            isCapturing = isCapturing,
            onDismiss = { showQuickCapture = false },
            onCapture = viewModel::captureIntoNote,
        )
    }
}

/** 输入栏「+」弹出的选择面板：拍照补充 / 从相册选择。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttachActionSheet(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickImages: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 12.dp),
        ) {
            AttachRow(
                icon = Icons.Default.CameraAlt,
                title = "拍一张",
                subtitle = "用相机补充当前状态，直接加到这条备忘",
                onClick = onTakePhoto,
            )
            AttachRow(
                icon = Icons.Default.PhotoLibrary,
                title = "从相册选择",
                subtitle = "最多 9 张，会复制一份到备忘里",
                onClick = onPickImages,
            )
        }
    }
}

@Composable
private fun AttachRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
