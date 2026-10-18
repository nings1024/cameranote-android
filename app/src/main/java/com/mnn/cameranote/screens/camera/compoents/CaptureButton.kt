package com.mnn.cameranote.screens.camera.compoents

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * 快门按钮。相机页和「笔记里补拍」弹窗共用。
 *
 * 保存过程中内圈缩小并显示进度，同时禁用点击，天然防连点。
 */
@Composable
fun CaptureButton(
    isCapturing: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val innerScale by animateFloatAsState(
        targetValue = if (isCapturing) 0.72f else 1f,
        label = "captureInnerScale",
    )

    Box(
        modifier = modifier
            .size(84.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.25f))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "拍照" },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(66.dp)
                .scale(innerScale)
                .clip(CircleShape)
                .background(if (enabled) Color.White else Color.White.copy(alpha = 0.5f)),
        )
        if (isCapturing) {
            CircularProgressIndicator(
                modifier = Modifier.size(30.dp),
                strokeWidth = 3.dp,
                color = Color.Black.copy(alpha = 0.55f),
            )
        }
    }
}
