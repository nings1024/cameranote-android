package com.mnn.cameranote.data.manager

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 把应用私有目录里的照片导出到系统相册（MediaStore）。
 *
 * 用途：让「重要的图片」能进入系统相册，从而被其他应用 / 云同步 / 用户手动备份看到。
 * 应用自身的记录仍然只留在私有目录里。
 *
 * Android 10 以下写入相册需要 WRITE_EXTERNAL_STORAGE 运行时权限，由 UI 层负责申请。
 */
class GallerySaver(private val context: Context) {

    suspend fun saveImage(sourcePath: String): Result<Uri> = withContext(Dispatchers.IO) {
        val source = File(sourcePath)
        if (!source.isFile || source.length() <= 0L) {
            return@withContext Result.failure(IOException("图片文件不存在"))
        }

        val resolver = context.contentResolver
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "相记_$stamp.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}${File.separator}相记",
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        var uri: Uri? = null
        try {
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return@withContext Result.failure(IOException("无法创建相册记录"))

            val output = resolver.openOutputStream(uri)
                ?: return@withContext Result.failure(IOException("无法写入相册"))
            output.use { target ->
                source.inputStream().use { input -> input.copyTo(target) }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            Result.success(uri)
        } catch (t: Throwable) {
            Log.w(TAG, "保存到相册失败", t)
            uri?.let { runCatching { resolver.delete(it, null, null) } }
            Result.failure(t)
        }
    }

    companion object {
        private const val TAG = "GallerySaver"

        /** Android 10 以下写入相册需要该权限。 */
        fun requiresLegacyPermission(): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    }
}
