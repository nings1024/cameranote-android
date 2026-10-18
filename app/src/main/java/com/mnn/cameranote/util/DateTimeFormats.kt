package com.mnn.cameranote.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * 时间格式化。
 *
 * 全部使用预编译的 [DateTimeFormatter] 单例——之前是在 LazyColumn 的每一项里
 * 现 new 一个 `DateTimeFormatter`，列表一滚动就产生大量临时对象。
 */
private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val MONTH_DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd")
private val FULL_DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
private val CLOCK_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")

/** 列表右上角的时间：今天只显示时分，昨天显示「昨天」，今年省略年份。 */
fun Long.toListTimeString(zoneId: ZoneId = ZoneId.systemDefault()): String {
    val dateTime = Instant.ofEpochMilli(this).atZone(zoneId)
    val today = LocalDate.now(zoneId)
    return when {
        dateTime.toLocalDate() == today -> TIME_FORMATTER.format(dateTime)
        dateTime.toLocalDate() == today.minusDays(1) -> "昨天"
        dateTime.year == today.year -> MONTH_DAY_FORMATTER.format(dateTime)
        else -> FULL_DATE_TIME_FORMATTER.format(dateTime)
    }
}

/** 聊天气泡右下角的完整时间。 */
fun Long.toChatTimeString(zoneId: ZoneId = ZoneId.systemDefault()): String =
    CLOCK_FORMATTER.format(Instant.ofEpochMilli(this).atZone(zoneId))

/**
 * Material3 DatePicker 返回的是 **UTC 当天 0 点** 的毫秒值。
 *
 * 直接当作本地时间格式化会差一天（东八区会变成前一天 08:00），
 * 这里统一按 UTC 取年月日，再换算成设备时区的 [dayStart, dayEnd) 区间。
 */
fun utcDateMillisToLocalDayRange(utcMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): Pair<Long, Long> {
    val utcDate = Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
    val start = utcDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val end = utcDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    return start to end
}

/** 展示用的日期文本，例如 `2026-03-05`。 */
fun formatDayLabel(dayStart: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
    MONTH_DAY_FORMATTER.format(Instant.ofEpochMilli(dayStart).atZone(zoneId))

/** 今天 / 明天的本地 0 点，用于「今天」快捷筛选。 */
fun todayRange(zoneId: ZoneId = ZoneId.systemDefault()): Pair<Long, Long> {
    val today = LocalDate.now(zoneId)
    return today.atStartOfDay(zoneId).toInstant().toEpochMilli() to
        today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
}

/** 本周（周一起）的区间，用于「本周」快捷筛选。 */
fun thisWeekRange(zoneId: ZoneId = ZoneId.systemDefault()): Pair<Long, Long> {
    val today = LocalDate.now(zoneId)
    val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    return monday.atStartOfDay(zoneId).toInstant().toEpochMilli() to
        today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
}
