package com.mnn.cameranote.screens.messagedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mnn.cameranote.data.local.entity.MessageItemEntity
import com.mnn.cameranote.ui.image.imageRequest
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

/**
 * 全屏看图 + 左右翻页。
 *
 * 每个页面按需解码到 2048px，避免翻页时把多张 12MP 原图同时解进内存。
 */
@Composable
fun FullScreenImagePager(
    images: List<MessageItemEntity>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    onSaveClick: (MessageItemEntity) -> Unit,
) {
    if (images.isEmpty()) return

    val context = LocalContext.current
    val safeIndex = initialIndex.coerceIn(0, images.lastIndex)
    val pagerState = rememberPagerState(initialPage = safeIndex) { images.size }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 16.dp,
            ) { pageIndex ->
                val item = images[pageIndex]
                ZoomableAsyncImage(
                    model = imageRequest(context, item.content, sizePx = 2048),
                    contentDescription = "第 ${pageIndex + 1} 张图片",
                    modifier = Modifier.fillMaxSize(),
                    state = rememberZoomableImageState(rememberZoomableState()),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), shape = androidx.compose.foundation.shape.CircleShape)) {
                    Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.White)
                }

                Text(
                    text = "${pagerState.currentPage + 1} / ${images.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                )

                IconButton(
                    onClick = { onSaveClick(images[pagerState.currentPage]) },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), shape = CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.SaveAlt,
                        contentDescription = "保存到相册",
                        tint = Color.White,
                    )
                }
            }
        }
    }
}
