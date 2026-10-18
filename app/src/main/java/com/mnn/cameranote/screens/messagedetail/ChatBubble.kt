package com.mnn.cameranote.screens.messagedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mnn.cameranote.data.local.entity.MessageItemEntity
import com.mnn.cameranote.data.local.entity.MessageItemType
import com.mnn.cameranote.ui.image.imageRequest
import com.mnn.cameranote.util.toChatTimeString

/**
 * 详情页的一条内容。
 *
 * 点击图片 = 全屏预览；长按图片 = 直接保存到系统相册（给「重要图片」一个快速出口）。
 */
@Composable
fun ChatBubble(
    message: MessageItemEntity,
    onImageClick: (MessageItemEntity) -> Unit,
    onImageLongClick: (MessageItemEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isImage = message.type == MessageItemType.IMAGE
    val context = LocalContext.current
    val request = remember(message.content) {
        if (isImage) imageRequest(context, message.content, sizePx = 720) else null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isImage) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            tonalElevation = 0.dp,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .then(
                    if (isImage) {
                        Modifier.combinedClickable(
                            onClick = { onImageClick(message) },
                            onLongClick = { onImageLongClick(message) },
                            onLongClickLabel = "保存到相册",
                        )
                    } else {
                        Modifier
                    },
                ),
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                if (request != null) {
                    AsyncImage(
                        model = request,
                        contentDescription = "备忘图片，长按可保存到相册",
                        modifier = Modifier
                            .sizeIn(maxWidth = 240.dp, maxHeight = 320.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                Text(
                    text = message.createTime.toChatTimeString(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
    }
}
