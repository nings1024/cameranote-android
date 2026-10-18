package com.mnn.cameranote.screens.messagedetail

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mnn.cameranote.screens.camera.CameraPreview
import com.mnn.cameranote.screens.camera.SHUTTER_FLASH_MIN_MILLIS
import com.mnn.cameranote.screens.camera.compoents.CameraIconButton
import com.mnn.cameranote.screens.camera.compoents.CaptureButton
import kotlinx.coroutines.delay

/**
 * 全屏「补拍」弹窗：正在看某条备忘时，顺手用相机补一张照片。
 *
 * 直接复用相机页的 [CameraPreview]，所以点击对焦、双指缩放、闪光灯切换都一样；
 * 弹窗关闭时 `CameraPreview` 会主动解绑相机，不会留在后台空转。
 */
@Composable
fun QuickCaptureDialog(
    isCapturing: Boolean,
    onDismiss: () -> Unit,
    onCapture: (ImageCapture) -> Unit,
) {
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }
    var hasFlashUnit by remember { mutableStateOf(true) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var retryKey by remember { mutableIntStateOf(0) }
    var shutterFlash by remember { mutableStateOf(false) }

    // 只在快门瞬间闪一下白，而不是整个保存过程都盖着白纱
    LaunchedEffect(isCapturing) {
        if (isCapturing) {
            shutterFlash = true
            delay(SHUTTER_FLASH_MIN_MILLIS)
            shutterFlash = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                lensFacing = lensFacing,
                flashMode = flashMode,
                retryKey = retryKey,
                onImageCaptureReady = { imageCapture = it },
                onFlashAvailabilityChanged = { available ->
                    hasFlashUnit = available
                    if (!available) flashMode = ImageCapture.FLASH_MODE_OFF
                },
                onCameraReady = { cameraError = null },
                onError = { cameraError = it },
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CameraIconButton(onClick = onDismiss, contentDescription = "关闭") { tint ->
                        Icon(Icons.Default.Close, contentDescription = null, tint = tint)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CameraIconButton(
                            onClick = {
                                flashMode = when (flashMode) {
                                    ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                                    ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                                    else -> ImageCapture.FLASH_MODE_OFF
                                }
                            },
                            enabled = hasFlashUnit,
                            contentDescription = "闪光灯",
                        ) { tint ->
                            Icon(
                                imageVector = when (flashMode) {
                                    ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                                    ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                                    else -> Icons.Default.FlashOff
                                },
                                contentDescription = null,
                                tint = tint,
                            )
                        }

                        CameraIconButton(
                            onClick = {
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                    CameraSelector.LENS_FACING_FRONT
                                } else {
                                    CameraSelector.LENS_FACING_BACK
                                }
                            },
                            contentDescription = "切换前后摄像头",
                        ) { tint ->
                            Icon(Icons.Default.Cameraswitch, contentDescription = null, tint = tint)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                cameraError?.let { message ->
                    Text(
                        text = message,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                    )
                    TextButton(
                        onClick = {
                            cameraError = null
                            retryKey++
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text("重试")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CaptureButton(
                        isCapturing = isCapturing,
                        enabled = !isCapturing && cameraError == null && imageCapture != null,
                        onClick = { imageCapture?.let(onCapture) },
                    )
                }
            }

            // 快门反馈
            if (shutterFlash) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.28f)),
                )
            }
        }
    }
}
