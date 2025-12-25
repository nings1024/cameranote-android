package com.mnn.cameranote.screens.messagedetail

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    selectedImages: List<Uri>, // 新增：已选图片的 URI 列表
    onRemoveImage: (Uri) -> Unit, // 新增：删除预览图
    onSendClick: () -> Unit, // 修改：统一发送逻辑（文本+图片）
    onAddImageClick: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        modifier = Modifier.imePadding()
    ) {
        Column {
            // --- 图片预览区域 ---
            if (selectedImages.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedImages) { uri ->
                        Box(modifier = Modifier.size(72.dp)) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            // 删除按钮
                            IconButton(
                                onClick = { onRemoveImage(uri) },
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.TopEnd)
                                    .background(Color.Black.copy(0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, "删除", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // --- 原有的输入栏 ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onAddImageClick) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "选择图片", tint = MaterialTheme.colorScheme.primary)
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入消息...") },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSendClick,
                    // 只要有文字或者有图片，就可以点击发送
                    enabled = text.isNotBlank() || selectedImages.isNotEmpty(),
                    shape = CircleShape
                ) {
                    Text("发送")
                }
            }
        }
    }
}