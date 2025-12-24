package com.mnn.cameranote.screens.camera

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnn.cameranote.data.database.repository.MessageRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate.now
import java.util.concurrent.Executor

class CameraViewModel(private val repository: MessageRepository) : ViewModel() {
    // 状态管理
    var lensFacing by mutableIntStateOf(CameraSelector.LENS_FACING_BACK)
        private set

    var flashMode by mutableIntStateOf(ImageCapture.FLASH_MODE_OFF)
        private set

    var isFlashing by mutableStateOf(false)
        private set

    fun toggleCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
    }

    fun toggleFlash(imageCapture: ImageCapture) {
        flashMode = when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }
        imageCapture.flashMode = flashMode
    }

    /**
     * 拍照并保存图片的函数
     * @param context 上下文对象，用于获取应用私有目录
     * @param imageCapture 相机拍照功能实例
     * @param executor 线程执行器，用于在后台线程执行任务
     * @param onSuccess 拍照成功后的回调函数，接收保存的图片文件作为参数
     */
    fun capturePhoto(
        context: Context,
        imageCapture: ImageCapture,
        executor: Executor,
        onSuccess: (File) -> Unit
    ) {
        // 1. 获取私有目录路径: /Android/data/你的包名/files/Pictures
        val storageDir = context.createYearMonthDirectory()

        // 2. 创建文件名，使用当前时间戳确保文件名唯一性
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        // 根据目录和文件名创建文件对象
        val photoFile = File(storageDir, fileName)

        // 3. 使用文件直接创建输出选项，配置图片保存参数
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // 执行拍照操作，配置保存选项、执行器和回调
        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                // 图片保存成功的回调处理
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    viewModelScope.launch {
                        repository.createNewMessage(photoFile.absolutePath )
                    }

                    // 切换到主线程执行成功回调
                    Handler(Looper.getMainLooper()).post {
                        onSuccess(photoFile)
                    }
                }

                // 图片保存失败的回调处理
                override fun onError(exception: ImageCaptureException) {
                    // 记录错误日志
                    Log.e("Camera", "保存至私有路径失败: ${exception.message}")
                }
            }
        )
    }

    fun Context.createYearMonthDirectory(): File {

        val baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val yearDir = File(baseDir, now().year.toString())
        val monthDir = File(yearDir, now().month.value.toString())

        if (!monthDir.exists()) {
            monthDir.mkdirs()
        }

        return monthDir
    }

}