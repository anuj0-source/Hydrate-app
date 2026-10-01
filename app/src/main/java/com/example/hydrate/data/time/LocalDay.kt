package com.example.hydrate.data.time

import java.util.Calendar
import java.util.Locale

/**
 * Local calendar-day helpers shared by water logging, the home screen, alarms, and widgets.
 *
 * Water intake is grouped by the user's local day. Keeping the date-key calculation in one
 * place prevents a screen that remains open across midnight from continuing to use yesterday's
 * key.
 */
object LocalDay {
    fun keyAt(timeMillis: Long = System.currentTimeMillis()): String {
        val calendar = Calendar.getInstance().apply { this.timeInMillis = timeMillis }
        return String.format(
            Locale.US,
            "%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun millisUntilNextMidnight(timeMillis: Long = System.currentTimeMillis()): Long {
        val nextMidnight = Calendar.getInstance().apply {
            this.timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1)
        }
        return (nextMidnight.timeInMillis - timeMillis).coerceAtLeast(1)
    }
}
