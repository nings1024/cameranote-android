package com.mnn.cameranote.screens.messagedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mnn.cameranote.database.entity.MessageItemEntity
import com.mnn.cameranote.database.entity.MessageType
import com.mnn.cameranote.screens.messagelist.toDateTimeString

@Composable
fun ChatBubble(message: MessageItemEntity) {
    var showFullScreen by remember { mutableStateOf(false) }

    if (showFullScreen && message.type == MessageType.IMAGE.ordinal) {
        FullScreenImagePreview(
            imagePath = message.content, // 记得转为 File 对象
            onDismiss = { showFullScreen = false }
        )
    }
    val isSelf = true
    val alignment = if (isSelf) Alignment.End else Alignment.Start
    val bubbleColor =
        if (isSelf) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = bubbleColor,
            tonalElevation = 2.dp,
            // 如果是图片，让整个气泡支持点击查看大图
            onClick = {
                if (message.type == MessageType.IMAGE.ordinal) showFullScreen = true
            }
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // 根据类型显示图片或文字
                if (message.type == MessageType.IMAGE.ordinal && message.content != null) {
                    AsyncImage(
                        model = message.content,
                        contentDescription = null,
                        modifier = Modifier.sizeIn(maxWidth = 200.dp, maxHeight = 300.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(text = message.content, style = MaterialTheme.typography.bodyLarge)
                }

                // 时间显示
                Text(
                    text = message.createTime.toDateTimeString(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}