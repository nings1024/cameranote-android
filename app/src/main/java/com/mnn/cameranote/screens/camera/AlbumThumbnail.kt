package com.mnn.cameranote.screens.camera
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun AlbumThumbnail(
    lastPhotoUri: Any?, // 可以是 File, Uri 或 URL
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .padding(16.dp) // 距离边缘的间距
            .size(60.dp)    // 缩略图大小
            .border(2.dp, Color.White, CircleShape), // 白色边圈，增加质感
        shape = RoundedCornerShape(16.dp),
        color = Color.Gray // 无图时的背景色
    ) {
        if (lastPhotoUri != null) {
            AsyncImage(
                model = lastPhotoUri,
                contentDescription = "最近照片",
                contentScale = ContentScale.Crop, // 裁剪填满圆形
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // 这里可以放一个默认的相册图标
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.padding(16.dp),
                tint = Color.White
            )
        }
    }
}