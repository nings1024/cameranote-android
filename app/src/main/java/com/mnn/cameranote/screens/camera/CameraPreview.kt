package com.mnn.cameranote.screens.camera

import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    flashMode: Int = ImageCapture.FLASH_MODE_OFF,
    onImageCaptureReady: (ImageCapture?) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    // 监听镜头方向和生命周期的变化
    LaunchedEffect(lensFacing, lifecycleOwner) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).await() // 建议使用 ListenableFuture 扩展

        // 构建预览配置
        val preview = Preview.Builder().build().apply {
            surfaceProvider = previewView.surfaceProvider
        }

        // 构建拍照配置
        val imageCapture = ImageCapture.Builder()
            .setFlashMode(flashMode)
            .setTargetRotation(previewView.display?.rotation ?: Surface.ROTATION_0)
            .build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            onImageCaptureReady(imageCapture)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 单独监听闪光灯模式变化，无需重新绑定整个相机
    LaunchedEffect(flashMode) {
        // 这里的逻辑通常需要通过已绑定的 imageCapture 实例修改
        // 在 ViewModel 模式下，直接修改 imageCapture 对象的属性即可
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}