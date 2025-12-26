// CameraScreen.kt
package com.mnn.cameranote.screens.camera

import android.Manifest
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.mnn.cameranote.screens.camera.compoents.AlbumThumbnail
import com.mnn.cameranote.screens.camera.compoents.CameraControls
import com.mnn.cameranote.screens.camera.compoents.NoteInputDialog
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(onNavigateToGallery: () -> Unit, viewModel: CameraViewModel = koinViewModel()) {
    val context = LocalContext.current
    val executor = remember { ContextCompat.getMainExecutor(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

//    获取最后一张图片
    val lastPhotoUri by viewModel.lastPhoto.collectAsState()

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
                        lastPhotoUri = lastPhotoUri,
                        onClick = { onNavigateToGallery() }
                    )

                    // 中间拍照按钮
                    CaptureButton {
                        imageCapture?.let {
                            viewModel.capturePhoto(context, it, executor) {

                            }
                        }
                    }
                    // 这里把原来的 Spacer 换成备注按钮
                    IconButton(onClick = { viewModel.showNoteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "备注",
                            tint = Color.White
                        )
                    }
                }
            }

            // 快门闪烁动画
            if (viewModel.isFlashing) {
                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.5f)))
            }
            NoteInputDialog(
                isVisible = viewModel.showNoteDialog,
                noteText = viewModel.photoNote,
                onNoteChange = { viewModel.photoNote = it },
                onDismiss = {
                    viewModel.showNoteDialog = false
                    viewModel.updateNote()
                }
            )
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