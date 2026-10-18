package com.mnn.cameranote.screens.camera

import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlin.math.roundToInt

private const val TAG = "CameraPreview"

/**
 * 相机预览。
 *
 * 关键点：
 * - 绑定失败（无前置镜头 / 被其他应用占用 / 多窗口）会通过 [onError] 上报，而不是黑屏无反馈；
 * - 闪光灯模式由 [flashMode] 单向驱动到 [ImageCapture]，UI 图标与硬件状态不会再脱节；
 * - 支持点击对焦与双指缩放；
 * - 相机绑定在 NavBackStackEntry 的生命周期上，离开本页会自动释放；
 * - 被复用在全屏「补拍」弹窗里，离开组合时主动解绑，避免相机在后台空转。
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    flashMode: Int = ImageCapture.FLASH_MODE_OFF,
    retryKey: Int = 0,
    onImageCaptureReady: (ImageCapture?) -> Unit = {},
    onFlashAvailabilityChanged: (Boolean) -> Unit = {},
    onCameraReady: () -> Unit = {},
    onError: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    var isFocusing by remember { mutableStateOf(false) }
    var minZoom by remember { mutableFloatStateOf(1f) }
    var maxZoom by remember { mutableFloatStateOf(1f) }

    // 每个镜头一个 ImageCapture：切换前后摄时重建，避免复用已解绑的用例
    val imageCapture = remember(lensFacing) {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    val focusScale by animateFloatAsState(
        targetValue = if (isFocusing) 1f else 1.5f,
        animationSpec = tween(durationMillis = 200),
        label = "focusRingScale",
    )

    // 闪光灯状态 -> 当前 ImageCapture
    LaunchedEffect(flashMode, imageCapture) {
        runCatching { imageCapture.flashMode = flashMode }
            .onFailure { Log.w(TAG, "设置闪光灯模式失败", it) }
    }

    // 绑定 / 重绑相机
    LaunchedEffect(lensFacing, lifecycleOwner, retryKey, imageCapture) {
        onImageCaptureReady(null)
        camera = null

        val provider = try {
            ProcessCameraProvider.getInstance(context).await()
        } catch (t: Throwable) {
            Log.w(TAG, "获取 CameraProvider 失败", t)
            onError("相机初始化失败")
            return@LaunchedEffect
        }
        cameraProvider = provider

        previewView.display?.let { display ->
            runCatching { imageCapture.targetRotation = display.rotation }
        }

        val preview = Preview.Builder().build().apply {
            surfaceProvider = previewView.surfaceProvider
        }
        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        try {
            provider.unbindAll()
            val bound = provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
            camera = bound

            val zoomState = bound.cameraInfo.zoomState.value
            minZoom = zoomState?.minZoomRatio ?: 1f
            maxZoom = zoomState?.maxZoomRatio ?: 1f

            onFlashAvailabilityChanged(bound.cameraInfo.hasFlashUnit())
            onImageCaptureReady(imageCapture)
            onCameraReady()
        } catch (t: Throwable) {
            Log.w(TAG, "绑定相机失败 lensFacing=$lensFacing", t)
            onImageCaptureReady(null)
            onError(
                if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    "当前设备没有可用的前置摄像头"
                } else {
                    "相机不可用，可能被其他应用占用"
                },
            )
        }
    }

    // 对焦框自动消失
    LaunchedEffect(focusPoint) {
        if (focusPoint != null) {
            isFocusing = true
            delay(120)
            isFocusing = false
            delay(700)
            focusPoint = null
        }
    }

    // 离开组合时立刻解绑：全屏补拍弹窗关闭后相机不能留在后台空转
    DisposableEffect(Unit) {
        onDispose {
            onImageCaptureReady(null)
            runCatching { cameraProvider?.unbindAll() }
                .onFailure { Log.w(TAG, "解绑相机失败", it) }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val cam = camera ?: return@detectTapGestures
                    focusPoint = offset
                    val point = previewView.meteringPointFactory.createPoint(offset.x, offset.y)
                    val action = FocusMeteringAction
                        .Builder(
                            point,
                            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE,
                        )
                        .setAutoCancelDuration(3, TimeUnit.SECONDS)
                        .build()
                    runCatching { cam.cameraControl.startFocusAndMetering(action) }
                        .onFailure { Log.w(TAG, "对焦失败", it) }
                }
            }
            .pointerInput(camera) {
                val cam = camera ?: return@pointerInput
                detectTransformGestures { _, _, zoomChange, _ ->
                    if (zoomChange == 1f) return@detectTransformGestures
                    val current = cam.cameraInfo.zoomState.value?.zoomRatio ?: return@detectTransformGestures
                    val target = (current * zoomChange).coerceIn(minZoom, maxZoom)
                    runCatching { cam.cameraControl.setZoomRatio(target) }
                }
            },
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize(),
        )

        focusPoint?.let { point ->
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (point.x - 40.dp.roundToPx()).roundToInt(),
                            (point.y - 40.dp.roundToPx()).roundToInt(),
                        )
                    }
                    .size(80.dp)
                    .scale(focusScale)
                    .border(1.5.dp, Color(0xFFFFD600)),
            )
        }
    }
}
