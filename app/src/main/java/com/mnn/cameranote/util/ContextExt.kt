package com.mnn.cameranote.util

import android.content.Context
import android.os.Environment
import java.io.File
import java.time.LocalDate.now

fun Context.createYearMonthDirectory(): File {

    val baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val yearDir = File(baseDir, now().year.toString())
    val monthDir = File(yearDir, now().month.value.toString())

    if (!monthDir.exists()) {
        monthDir.mkdirs()
    }

    return monthDir
}