package com.mnn.cameranote.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class DateTimeFormatsTest {

    /** DatePicker 给的是 UTC 当天 0 点。 */
    private fun utcMidnight(date: LocalDate): Long =
        date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    @Test
    fun `东八区换算到当天零点`() {
        val date = LocalDate.of(2026, 3, 5)
        val zone = ZoneId.of("Asia/Shanghai")

        val (start, end) = utcDateMillisToLocalDayRange(utcMidnight(date), zone)

        assertEquals(date.atStartOfDay(zone).toInstant().toEpochMilli(), start)
        assertEquals(24 * 60 * 60 * 1000L, end - start)
    }

    @Test
    fun `西八区不会差一天`() {
        val date = LocalDate.of(2026, 3, 5)
        val zone = ZoneId.of("America/Los_Angeles")

        val (start, _) = utcDateMillisToLocalDayRange(utcMidnight(date), zone)

        assertEquals(date.atStartOfDay(zone).toInstant().toEpochMilli(), start)
    }

    @Test
    fun `区间是左闭右开`() {
        val date = LocalDate.of(2026, 1, 1)
        val zone = ZoneId.of("UTC")
        val (start, end) = utcDateMillisToLocalDayRange(utcMidnight(date), zone)

        assertTrue(start <= utcMidnight(date))
        assertTrue(utcMidnight(date) < end)
    }

    @Test
    fun `今天的时间戳只显示时分`() {
        val text = System.currentTimeMillis().toListTimeString()
        assertTrue("实际输出: $text", Regex("""\d{2}:\d{2}""").matches(text))
    }
}
