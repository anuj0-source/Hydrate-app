package com.example.hydrate.data.time

import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalDayTest {
    @Test
    fun keyAtUsesTheLocalCalendarDay() {
        withUtcTimeZone {
            val time = utcTime(2026, Calendar.JANUARY, 15, 23, 59, 59)

            assertEquals("2026-01-15", LocalDay.keyAt(time))
        }
    }

    @Test
    fun millisUntilNextMidnightEndsAtTheFollowingDay() {
        withUtcTimeZone {
            val time = utcTime(2026, Calendar.JANUARY, 15, 23, 59, 59)

            assertEquals(1_000L, LocalDay.millisUntilNextMidnight(time))
        }
    }

    private fun utcTime(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int
    ): Long = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(year, month, day, hour, minute, second)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private inline fun withUtcTimeZone(block: () -> Unit) {
        val originalTimeZone = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            block()
        } finally {
            TimeZone.setDefault(originalTimeZone)
        }
    }
}
