package com.mnn.cameranote.screens.camera

import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 把 CameraX 的回调式拍照包成挂起函数，ViewModel 里就能用 try/catch/finally
 * 表达「成功落库 / 失败清理 / 无论如何复位状态」。
 */
suspend fun ImageCapture.takePictureSuspend(
    targetFile: File,
    executor: Executor,
): ImageCapture.OutputFileResults = suspendCancellableCoroutine { continuation ->
    val options = ImageCapture.OutputFileOptions.Builder(targetFile).build()
    takePicture(
        options,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                if (continuation.isActive) continuation.resume(outputFileResults)
            }

            override fun onError(exception: ImageCaptureException) {
                if (continuation.isActive) continuation.resumeWithException(exception)
            }
        },
    )
}
