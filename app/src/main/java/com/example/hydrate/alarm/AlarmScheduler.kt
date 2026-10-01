package com.example.hydrate.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.hydrate.data.db.HydrateDatabase
import com.example.hydrate.data.prefs.SettingsRepository
import com.example.hydrate.data.time.LocalDay
import kotlinx.coroutines.flow.first
import java.util.Calendar

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun scheduleNextAlarm(
        startTimeMins: Int,
        endTimeMins: Int,
        intervalMins: Int,
        skipNextScheduledReminder: Boolean = false
    ) {
        val repo = SettingsRepository(context)
        if (!repo.isAlarmEnabled.first()) {
            cancelAlarm()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("AlarmScheduler", "Cannot schedule exact alarms - missing permission")
                return
            }
        }

        val db = HydrateDatabase.getDatabase(context)
        val today = LocalDay.keyAt()

        val totalWater = db.hydrateDao().getTotalWaterForDate(today).first() ?: 0
        val dailyGoal = repo.dailyGoalMl.first()
        val isGoalReached = totalWater >= dailyGoal

        val now = Calendar.getInstance()
        val currentMins = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val nextAlarmCal = Calendar.getInstance()

        val safeIntervalMins = intervalMins.coerceAtLeast(1)
        var targetMins = startTimeMins

        if (isGoalReached) {
            Log.d("AlarmScheduler", "Daily goal reached ($totalWater / $dailyGoal ml). Scheduling next alarm for tomorrow at start time.")
            nextAlarmCal.add(Calendar.DAY_OF_YEAR, 1)
        } else {
            // Calculate next alarm time today
            while (targetMins <= currentMins) {
                targetMins += safeIntervalMins
            }

            if (skipNextScheduledReminder) {
                // A manual drink satisfies the upcoming reminder slot, so move to the
                // following scheduled interval instead of alarming again right away.
                targetMins += safeIntervalMins
            }

            if (targetMins > endTimeMins) {
                // Schedule for tomorrow at start time
                targetMins = startTimeMins
                nextAlarmCal.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        nextAlarmCal.set(Calendar.HOUR_OF_DAY, targetMins / 60)
        nextAlarmCal.set(Calendar.MINUTE, targetMins % 60)
        nextAlarmCal.set(Calendar.SECOND, 0)
        nextAlarmCal.set(Calendar.MILLISECOND, 0)

        // Ensure we don't schedule in the past
        if (nextAlarmCal.timeInMillis <= System.currentTimeMillis()) {
            nextAlarmCal.add(Calendar.MINUTE, safeIntervalMins)
        }

        val pendingIntent = getPendingIntent()

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextAlarmCal.timeInMillis,
                pendingIntent
            )
            Log.d("AlarmScheduler", "Next alarm scheduled for: ${nextAlarmCal.time}")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Missing exact alarm permission", e)
        }
    }

    fun scheduleSnooze(snoozeMins: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return
        }

        val nextAlarmCal = Calendar.getInstance().apply {
            add(Calendar.MINUTE, snoozeMins)
        }

        val pendingIntent = getPendingIntent()
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextAlarmCal.timeInMillis,
                pendingIntent
            )
            Log.d("AlarmScheduler", "Snoozed alarm scheduled for: ${nextAlarmCal.time}")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Missing exact alarm permission", e)
        }
    }

    fun scheduleTestAlarm(seconds: Int) {
        val nextAlarmCal = Calendar.getInstance().apply {
            add(Calendar.SECOND, seconds)
        }
        val pendingIntent = getPendingIntent()
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextAlarmCal.timeInMillis,
                pendingIntent
            )
            Log.d("AlarmScheduler", "Test alarm scheduled in $seconds seconds")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Missing exact alarm permission", e)
        }
    }

    fun cancelAlarm() {
        alarmManager.cancel(getPendingIntent())
        Log.d("AlarmScheduler", "Alarm canceled")
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ALARM_REQUEST_CODE = 1001
    }
}
