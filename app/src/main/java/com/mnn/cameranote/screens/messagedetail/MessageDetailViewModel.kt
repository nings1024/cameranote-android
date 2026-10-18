package com.mnn.cameranote.screens.messagedetail

import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mnn.cameranote.data.local.entity.MessageEntity
import com.mnn.cameranote.data.local.entity.MessageItemEntity
import com.mnn.cameranote.data.local.entity.MessageItemType
import com.mnn.cameranote.data.manager.FileStore
import com.mnn.cameranote.data.manager.GallerySaver
import com.mnn.cameranote.data.repository.MessageRepository
import com.mnn.cameranote.navigation.Route
import com.mnn.cameranote.screens.camera.takePictureSuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.channels.Channel
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
import java.util.concurrent.Executor

/** 详情页的一次性提示 / 事件。 */
sealed interface DetailEvent {
    data class Message(val text: String) : DetailEvent

    /** 补拍的照片已经加入这条备忘（用于关闭补拍弹窗）。 */
    data object PhotoAdded : DetailEvent
}

class MessageDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: MessageRepository,
    private val gallerySaver: GallerySaver,
    private val fileStore: FileStore,
) : ViewModel() {

    /**
     * 详情页的 ViewModel 会被「详情 / 信息设置 / 编辑标题 / 编辑描述」四处复用，
     * 各自的路由类型并不相同，所以这里显式按参数名取 id，而不是假设当前路由是 Detail。
     */
    private val messageId: Long = runCatching {
        savedStateHandle.toRoute<Route.Detail>().id
    }.getOrElse {
        savedStateHandle.get<Long>(ARG_MESSAGE_ID) ?: 0L
    }

    val message: StateFlow<MessageEntity?> = repository.observeMessage(messageId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val messageItems: StateFlow<List<MessageItemEntity>> = repository.observeItems(messageId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    // ------------------------------------------------------------ 输入状态

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    /** 补拍弹窗里的快门遮罩状态 */
    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    private val _events = Channel<DetailEvent>(Channel.BUFFERED)
    val events: Flow<DetailEvent> = _events.receiveAsFlow()

    private val callbackExecutor: Executor = Dispatchers.Main.asExecutor()

    fun onInputChange(text: String) {
        _input.value = text
    }

    fun onImagesPicked(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val current = _selectedImages.value
        val room = (MAX_IMAGES - current.size).coerceAtLeast(0)
        if (room == 0) {
            sendMessage("一次最多添加 $MAX_IMAGES 张图片")
            return
        }
        if (uris.size > room) {
            sendMessage("一次最多添加 $MAX_IMAGES 张图片，已保留前 $room 张")
        }
        _selectedImages.value = current + uris.take(room)
    }

    fun onImageRemoved(uri: Uri) {
        _selectedImages.update { list -> list.filterNot { it == uri } }
    }

    /**
     * 发送当前输入。
     *
     * 关键点：只有真正写库成功后才清空输入框，失败不丢用户写的东西；
     * 图片先于文字入库，成功一批就清一批，重试时不会重复插入。
     */
    fun submit() {
        val text = _input.value.trim()
        val images = _selectedImages.value
        if (text.isEmpty() && images.isEmpty()) return
        if (_isSending.value) return

        viewModelScope.launch {
            _isSending.value = true
            try {
                if (images.isNotEmpty()) {
                    val inserted = repository.appendUploadedImages(messageId, images)
                    if (inserted > 0) _selectedImages.value = emptyList()
                }
                if (text.isNotEmpty()) {
                    repository.appendText(messageId, text)
                    _input.value = ""
                }
            } catch (t: Throwable) {
                Log.w(TAG, "发送失败", t)
                sendMessage("发送失败，请重试")
            } finally {
                _isSending.value = false
            }
        }
    }

    // ------------------------------------------------------------ 补拍

    /**
     * 在当前备忘里补拍一张照片（输入栏「+ → 拍一张」）。
     *
     * 复用相机页那套 `takePictureSuspend`：失败删半成品文件，
     * 无论成败都复位 capturing 状态，不会把快门锁死。
     */
    fun captureIntoNote(imageCapture: ImageCapture) {
        if (_isCapturing.value) return
        _isCapturing.value = true

        viewModelScope.launch {
            val target = try {
                withContext(Dispatchers.IO) { fileStore.newPhotoFile() }
            } catch (t: Throwable) {
                Log.w(TAG, "创建照片文件失败", t)
                null
            }

            if (target == null) {
                sendMessage("无法创建照片文件，请检查存储空间")
                _isCapturing.value = false
                return@launch
            }

            try {
                imageCapture.takePictureSuspend(target, callbackExecutor)
                repository.appendPhotoToMessage(messageId, target.absolutePath)
                sendEvent(DetailEvent.PhotoAdded)
            } catch (t: Throwable) {
                Log.w(TAG, "补拍失败", t)
                target.delete()
                sendMessage("拍照失败，请重试")
            } finally {
                _isCapturing.value = false
            }
        }
    }

    // ------------------------------------------------------------ 导出

    /** 把这张照片复制一份到系统相册。 */
    fun saveToGallery(item: MessageItemEntity) {
        if (item.type != MessageItemType.IMAGE) return
        viewModelScope.launch {
            gallerySaver.saveImage(item.content)
                .onSuccess { sendMessage("已保存到系统相册") }
                .onFailure {
                    Log.w(TAG, "保存到相册失败", it)
                    sendMessage("保存到相册失败")
                }
        }
    }

    // ------------------------------------------------------------ 编辑（挂起，等写完再返回）

    /**
     * 这些操作是 suspend 的：调用方在**写库完成之后**才导航返回，
     * 避免 popBackStack 把 viewModelScope 取消掉导致更新丢失。
     */
    suspend fun updateTitle(title: String) {
        repository.updateTitle(title, messageId)
    }

    suspend fun updateDetail(detail: String) {
        repository.updateDetail(detail, messageId)
    }

    suspend fun deleteMessage() {
        repository.deleteMessage(messageId)
    }

    private fun sendEvent(event: DetailEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private fun sendMessage(text: String) = sendEvent(DetailEvent.Message(text))

    private companion object {
        const val TAG = "MessageDetailVM"
        const val ARG_MESSAGE_ID = "id"
        const val MAX_IMAGES = 9
    }
}
