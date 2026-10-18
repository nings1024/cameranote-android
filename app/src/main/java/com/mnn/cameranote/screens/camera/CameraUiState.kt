package com.mnn.cameranote.screens.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture

/**
 * 相机页的单一 UI 状态。
 *
 * 设计理念第一条是「拍照要快」，所以这里的字段都服务于即时反馈：
 * 是否正在保存（防连点）、快门白闪、当前草稿已收了几个条目。
 */
data class CameraUiState(
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    val flashMode: Int = ImageCapture.FLASH_MODE_OFF,
    val hasFlashUnit: Boolean = true,
    val isCapturing: Boolean = false,
    /** 快门白闪 */
    val shutterFlashVisible: Boolean = false,
    val isNoteSheetVisible: Boolean = false,
    /** 备注草稿。放在 ViewModel 里，误触关闭面板也不会丢。 */
    val noteDraft: String = "",
    /** 当前草稿已收录的条目数 */
    val draftItemCount: Int = 0,
    /** 相机不可用时的提示文案 */
    val cameraError: String? = null,
) {
    val canCapture: Boolean get() = !isCapturing && cameraError == null

    val hasDraftContent: Boolean get() = draftItemCount > 0
}

/** 一次性事件（Snackbar / Toast），避免用持久状态表达瞬时反馈。 */
sealed interface CameraEvent {
    data class Message(val text: String) : CameraEvent

    companion object {
        fun message(text: String) = Message(text)
    }
}

/** 快门白闪的最短可见时长，避免快机型上闪不出来。 */
internal const val SHUTTER_FLASH_MIN_MILLIS = 120L
