package com.mnn.cameranote.screens.messagedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mnn.cameranote.data.local.entity.MessageItemEntity
import com.mnn.cameranote.data.local.entity.MessageItemType
import com.mnn.cameranote.screens.messagelist.toDateTimeString

// 1. 定义一个简单的点击回调参数
@Composable
fun ChatBubble(
    message: MessageItemEntity,
    onImageClick: (MessageItemEntity) -> Unit // 提升事件
) {
    val isSelf = true // 实际开发建议从 message 对象中获取
    val alignment = if (isSelf) Alignment.End else Alignment.Start
    val bubbleColor = if (isSelf)
        MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = bubbleColor,
            tonalElevation = 1.dp,
            // 只有图片才响应点击，且逻辑外传
            onClick = {
                if (message.type == MessageItemType.IMAGE) {
                    onImageClick(message)
                }
            },
            enabled = message.type == MessageItemType.IMAGE
        ) {
            // 使用内建优化：如果内部内容没变，Surface 会跳过重组
            BubbleContent(message)
        }
    }
}

@Composable
private fun BubbleContent(message: MessageItemEntity) {
    Column(modifier = Modifier.padding(10.dp)) {
        if (message.type == MessageItemType.IMAGE) {
            AsyncImage(
                model = message.content, // 如果是本地路径，Coil 能完美处理
                contentDescription = "图片消息",
                modifier = Modifier
                    .sizeIn(maxWidth = 240.dp, maxHeight = 320.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(text = message.content, style = MaterialTheme.typography.bodyLarge)
        }

        Text(
            text = message.createTime.toDateTimeString(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}