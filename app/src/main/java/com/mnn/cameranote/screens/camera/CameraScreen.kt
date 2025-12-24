// CameraScreen.kt
package com.mnn.cameranote.screens.camera

import android.Manifest
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(onNavigateToGallery: () -> Unit, viewModel: CameraViewModel = koinViewModel()) {
    val context = LocalContext.current
    val executor = remember { ContextCompat.getMainExecutor(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                lensFacing = viewModel.lensFacing,
                flashMode = viewModel.flashMode,
                onImageCaptureReady = { imageCapture = it }
            )

            // UI 控件层
            Column(modifier = Modifier.fillMaxSize()) {
                CameraControls(
                    flashMode = viewModel.flashMode,
                    onFlashClick = { imageCapture?.let { viewModel.toggleFlash(it) } },
                    onSwitchClick = { viewModel.toggleCamera() }
                )

                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左下角相册
                    AlbumThumbnail(
                        lastPhotoUri = null,
                        onClick = { onNavigateToGallery() }
                    )

                    // 中间拍照按钮
                    CaptureButton {
                        imageCapture?.let {
                            viewModel.capturePhoto(context, it, executor) {
                                Toast.makeText(context, "保存${it.toString()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    // 右下角占位或添加其他按钮
                    Spacer(modifier = Modifier.size(48.dp)) //
                }
            }

            // 快门闪烁动画
            if (viewModel.isFlashing) {
                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.5f)))
            }
        } else {
            PermissionDeniedContent { cameraPermissionState.launchPermissionRequest() }
        }
    }
}

@Composable
fun CaptureButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(80.dp).clip(CircleShape).clickable { onClick() },
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(Modifier.fillMaxSize()) // 视觉上的圆圈
    }
}