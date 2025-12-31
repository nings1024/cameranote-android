package com.mnn.cameranote.data.manager

import android.content.Context
import android.net.Uri
import com.mnn.cameranote.util.createYearMonthDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// data/manager/FileManager.kt
class FileManager(private val context: Context) {
    suspend fun saveUriToPrivate(uri: Uri): String? {
        // 使用注入的 context 处理流拷贝
        return withContext(Dispatchers.IO) {
            val storageDir = context.createYearMonthDirectory()
            val fileName = "IMG_${System.currentTimeMillis()}_${(100..999).random()}.jpg"
            val destFile = File(storageDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                destFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream) // 核心操作：流拷贝
                }
            }
            destFile.absolutePath
        }
    }
}