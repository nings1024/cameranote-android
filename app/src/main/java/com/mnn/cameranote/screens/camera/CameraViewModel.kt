package com.mnn.cameranote.screens.camera

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnn.cameranote.data.manager.FileStore
import com.mnn.cameranote.data.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executor

/**
 * 相机页 ViewModel。
 *
 * 与 ViewModel 无关的 Android 依赖（Context / Executor）都被 FileStore 和
 * 主线程 executor 内化了，这里只操心「状态 + 业务调用」。
 */
class CameraViewModel(
    private val repository: MessageRepository,
    private val fileStore: FileStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _events = Channel<CameraEvent>(Channel.BUFFERED)
    val events: Flow<CameraEvent> = _events.receiveAsFlow()

    /** CameraX 回调需要 Executor，直接用主线程调度器，省掉往 UI 传 Context。 */
    private val callbackExecutor: Executor = Dispatchers.Main.asExecutor()

    /** 相机页左下角「最近一张照片」。 */
    val lastPhoto: StateFlow<File?> = repository.observeLastPhoto()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    init {
        viewModelScope.launch {
            repository.observeDraftItemCount().collect { count ->
                _uiState.update { if (it.draftItemCount == count) it else it.copy(draftItemCount = count) }
            }
        }
    }

    // ------------------------------------------------------------ 相机控制

    fun onLensSwitch() {
        if (_uiState.value.isCapturing) return
        _uiState.update { state ->
            state.copy(
                lensFacing = if (state.lensFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                },
                cameraError = null,
            )
        }
    }

    fun onFlashToggle() {
        val state = _uiState.value
        if (!state.hasFlashUnit) {
            sendEvent("这台设备没有闪光灯")
            return
        }
        _uiState.update {
            it.copy(
                flashMode = when (it.flashMode) {
                    ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                    ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                    else -> ImageCapture.FLASH_MODE_OFF
                },
            )
        }
    }

    fun onFlashAvailabilityChanged(available: Boolean) {
        _uiState.update {
            if (available) {
                it.copy(hasFlashUnit = true)
            } else {
                it.copy(hasFlashUnit = false, flashMode = ImageCapture.FLASH_MODE_OFF)
            }
        }
    }

    fun onCameraError(message: String) {
        _uiState.update { it.copy(cameraError = message) }
    }

    fun onCameraReady() {
        _uiState.update { if (it.cameraError == null) it else it.copy(cameraError = null) }
    }

    fun onRetryCamera() {
        _uiState.update { it.copy(cameraError = null) }
    }

    // ------------------------------------------------------------ 拍照

    /**
     * 拍照并落库。整个流程用 try/catch/finally 包住：
     * 失败删掉半成品文件，无论成败都会复位「正在保存」状态。
     */
    fun onCapture(imageCapture: ImageCapture?) {
        if (imageCapture == null) {
            sendEvent("相机还没准备好，请稍候")
            return
        }
        if (_uiState.value.isCapturing) return

        _uiState.update { it.copy(isCapturing = true) }
        triggerShutterFlash()

        viewModelScope.launch {
            val target = try {
                // 首次使用可能要建「年/月」目录，别在主线程做文件系统操作
                withContext(Dispatchers.IO) { fileStore.newPhotoFile() }
            } catch (t: Throwable) {
                Log.w(TAG, "创建照片文件失败", t)
                null
            }

            if (target == null) {
                sendEvent("无法创建照片文件，请检查存储空间")
                _uiState.update { it.copy(isCapturing = false) }
                return@launch
            }

            try {
                imageCapture.takePictureSuspend(target, callbackExecutor)
                repository.addCapturedPhoto(target.absolutePath)
            } catch (t: Throwable) {
                Log.w(TAG, "拍照保存失败", t)
                target.delete()
                sendEvent("拍照失败，请重试")
            } finally {
                _uiState.update { it.copy(isCapturing = false) }
            }
        }
    }

    private fun triggerShutterFlash() {
        viewModelScope.launch {
            _uiState.update { it.copy(shutterFlashVisible = true) }
            delay(SHUTTER_FLASH_MIN_MILLIS)
            _uiState.update { it.copy(shutterFlashVisible = false) }
        }
    }

    // ------------------------------------------------------------ 备注面板

    fun onOpenNoteSheet() {
        _uiState.update { it.copy(isNoteSheetVisible = true) }
    }

    fun onCloseNoteSheet() {
        // 关闭面板不清空已输入内容，误触才有后悔的余地
        _uiState.update { it.copy(isNoteSheetVisible = false) }
    }

    fun onNoteDraftChange(text: String) {
        _uiState.update { it.copy(noteDraft = text) }
    }

    /** 把当前草稿归档成一条带描述的备忘。 */
    fun onSaveNote() {
        val note = _uiState.value.noteDraft.trim()
        if (note.isEmpty()) {
            sendEvent("写点什么再保存吧")
            return
        }
        viewModelScope.launch {
            val saved = try {
                repository.saveNote(note)
            } catch (t: Throwable) {
                Log.w(TAG, "保存备注失败", t)
                false
            }
            _uiState.update { it.copy(isNoteSheetVisible = false) }
            if (saved) {
                _uiState.update { it.copy(noteDraft = "") }
                sendEvent("已归档：$note")
            } else {
                sendEvent("还没有拍任何内容")
            }
        }
    }

    private fun sendEvent(text: String) {
        viewModelScope.launch { _events.send(CameraEvent.message(text)) }
    }

    private companion object {
        const val TAG = "CameraViewModel"
    }
}
