package com.mnn.cameranote.screens.camera

import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
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

    // 核心状态：保存 Camera 控制对象和点击坐标
    var camera by remember { mutableStateOf<Camera?>(null) }
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    var isFocusing by remember { mutableStateOf(false) }

    // 对焦框缩放动画：点击时大，对焦中缩小
    val focusScale by animateFloatAsState(
        targetValue = if (isFocusing) 1f else 1.5f,
        animationSpec = tween(durationMillis = 200)
    )

    // 1. 相机绑定逻辑
    LaunchedEffect(lensFacing, lifecycleOwner) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).await()

        val preview = Preview.Builder().build().apply {
            surfaceProvider = previewView.surfaceProvider
        }

        val imageCapture = ImageCapture.Builder()
            .setFlashMode(flashMode)
            .build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            cameraProvider.unbindAll()
            // 获取绑定后的 camera 对象
            camera = cameraProvider.bindToLifecycle(
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

    // 2. 闪光灯模式动态更新
    LaunchedEffect(flashMode) {
        // 这里可以直接更新已有的 imageCapture，但由于它是内部变量，
        // 建议在 onImageCaptureReady 回调中处理，或者维持原本的重新绑定逻辑。
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            // 3. 手势监听：点击触发对焦
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    camera?.let { cam ->
                        focusPoint = offset
                        isFocusing = true

                        // 坐标转换：屏幕坐标 -> 相机测光点
                        val factory = previewView.meteringPointFactory
                        val point = factory.createPoint(offset.x, offset.y)
                        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
                            .setAutoCancelDuration(3, TimeUnit.SECONDS)
                            .build()

                        cam.cameraControl.startFocusAndMetering(action)
                    }
                }
            }
    ) {
        // 相机预览底层
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // 4. 视觉反馈：对焦框 UI
        focusPoint?.let { point ->
            Box(
                modifier = Modifier
                    .offset { IntOffset(point.x.toInt() - 40.dp.roundToPx(), point.y.toInt() - 40.dp.roundToPx()) }
                    .size(80.dp)
                    .scale(focusScale) // 缩放动画
                    .border(1.5.dp, Color(0xFFFFD600)) // 经典的相机黄色对焦框
            )

            // 1秒后自动消失
            LaunchedEffect(point) {
                delay(1000)
                focusPoint = null
                isFocusing = false
            }
        }
    }
}