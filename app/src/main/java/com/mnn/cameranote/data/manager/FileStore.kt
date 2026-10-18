package com.mnn.cameranote.data.manager

import android.content.Context
import android.net.Uri
import android.util.Log
import com.mnn.cameranote.util.createYearMonthDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.util.UUID

/**
 * 应用私有目录里的照片文件管理。
 *
 * 只负责「文件」本身，不涉及数据库；删除备忘时由 Repository 调用 [deleteQuietly]。
 */
class FileStore(private val context: Context) {

    @Volatile
    private var cachedDir: File? = null

    @Volatile
    private var cachedKey: String? = null

    /**
     * 生成一个新的照片文件（不创建文件本身，由 CameraX 写入）。
     *
     * 目录做了缓存，正常情况下不会在调用线程上做文件系统操作。
     */
    fun newPhotoFile(): File {
        val today = LocalDate.now()
        val key = "${today.year}-${today.monthValue}"
        val dir = if (cachedKey == key) {
            cachedDir ?: context.createYearMonthDirectory(today).also { cachedDir = it }
        } else {
            context.createYearMonthDirectory(today).also {
                cachedDir = it
                cachedKey = key
            }
        }
        val name = "IMG_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
        return File(dir, name)
    }

    /** 把相册里选中的图片拷贝进私有目录，失败返回 null（不会留下空文件）。 */
    suspend fun saveUriToPrivate(uri: Uri): String? = withContext(Dispatchers.IO) {
        val dest = newPhotoFile()
        try {
            val input = context.contentResolver.openInputStream(uri)
                ?: return@withContext null
            input.use { source ->
                dest.outputStream().use { target -> source.copyTo(target) }
            }
            if (dest.length() <= 0L) {
                dest.delete()
                null
            } else {
                dest.absolutePath
            }
        } catch (t: Throwable) {
            Log.w(TAG, "拷贝图片失败: ${uri}", t)
            dest.delete()
            null
        }
    }

    /** 删除磁盘文件，失败不抛异常。 */
    fun deleteQuietly(path: String?) {
        if (path.isNullOrBlank()) return
        try {
            val file = File(path)
            if (file.exists() && !file.delete()) {
                Log.w(TAG, "删除文件失败: $path")
            }
        } catch (t: Throwable) {
            Log.w(TAG, "删除文件异常: $path", t)
        }
    }

    companion object {
        private const val TAG = "FileStore"
    }
}
