package com.mnn.cameranote.util

import android.content.Context
import android.os.Environment
import java.io.File
import java.time.LocalDate

/**
 * 照片根目录。外部存储不可用时（极少数定制 ROM / 存储挂载异常）回退到内部目录，
 * 避免 `getExternalFilesDir` 返回 null 直接崩溃。
 */
fun Context.picturesRootDir(): File =
    getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: File(filesDir, "Pictures")

/** 按 `年/月` 分目录存放照片。 */
fun Context.createYearMonthDirectory(date: LocalDate = LocalDate.now()): File {
    val dir = File(File(picturesRootDir(), date.year.toString()), date.monthValue.toString())
    if (!dir.isDirectory && !dir.mkdirs() && !dir.isDirectory) {
        // 极端情况下外置存储只读，退回内部目录
        val fallback = File(File(File(filesDir, "Pictures"), date.year.toString()), date.monthValue.toString())
        if (!fallback.isDirectory) fallback.mkdirs()
        return fallback
    }
    return dir
}
