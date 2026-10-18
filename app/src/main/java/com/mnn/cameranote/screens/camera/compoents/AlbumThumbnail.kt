package com.mnn.cameranote.screens.camera.compoents

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mnn.cameranote.ui.image.imageRequest
import java.io.File

/** 相机页左下角的相册入口，显示最近一张照片。 */
@Composable
fun AlbumThumbnail(
    photo: File?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val request = remember(photo?.absolutePath) {
        photo?.let { imageRequest(context, it.absolutePath, sizePx = 180) }
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .size(60.dp)
            .border(2.dp, Color.White, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.35f),
        contentColor = Color.White,
    ) {
        if (request != null) {
            AsyncImage(
                model = request,
                contentDescription = "最近照片",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = "打开相册",
                modifier = Modifier.padding(14.dp),
                tint = Color.White,
            )
        }
    }
}
