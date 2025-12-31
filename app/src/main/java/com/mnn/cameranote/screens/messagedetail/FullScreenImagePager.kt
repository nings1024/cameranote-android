package com.mnn.cameranote.screens.messagedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mnn.cameranote.data.local.entity.MessageItemEntity
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import java.io.File

@Composable
fun FullScreenImagePager(
    images: List<MessageItemEntity>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    // 1. 初始化 PagerState
    val pagerState = rememberPagerState(initialPage = initialIndex) { images.size }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // 2. 左右滑动容器
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 16.dp, // 页面间距
                userScrollEnabled = true // 允许滑动
            ) { pageIndex ->
                val imagePath = images[pageIndex].content

                // 3. 每一个页面放置一个可缩放图片
                // 注意：为了防止缩放和滑动冲突，Telephoto 通常处理得很好
                ZoomableAsyncImage(
                    model = File(imagePath),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    state = rememberZoomableImageState(rememberZoomableState())
                )
            }

            // 顶部栏：返回按钮 + 计数器
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.White)
                }

                // 显示当前页数，例如 3/10
                Text(
                    text = "${pagerState.currentPage + 1} / ${images.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}