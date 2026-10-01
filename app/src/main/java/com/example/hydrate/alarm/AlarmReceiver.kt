package com.example.hydrate.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.hydrate.data.db.HydrateDatabase
import com.example.hydrate.data.prefs.SettingsRepository
import com.example.hydrate.data.time.LocalDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = HydrateDatabase.getDatabase(context)
                val repo = SettingsRepository(context)
                val scheduler = AlarmScheduler(context)
                if (!repo.isAlarmEnabled.first()) {
                    Log.d("AlarmReceiver", "Alarm is disabled. Ignoring the delivered alarm.")
                    scheduler.cancelAlarm()
                    return@launch
                }

                val today = LocalDay.keyAt()

                val totalWater = db.hydrateDao().getTotalWaterForDate(today).first() ?: 0
                val dailyGoal = repo.dailyGoalMl.first()

                val start = repo.startTimeMins.first()
                val end = repo.endTimeMins.first()
                val interval = repo.intervalMins.first()

                if (totalWater >= dailyGoal) {
                    Log.d("AlarmReceiver", "Daily goal already completed ($totalWater/$dailyGoal ml). Skipping alarm and rescheduling for tomorrow.")
                    scheduler.scheduleNextAlarm(start, end, interval)
                } else {
                    Log.d("AlarmReceiver", "Daily goal not met ($totalWater/$dailyGoal ml). Triggering alarm.")
                    // Keep the reminder cadence alive even if the user dismisses this alarm.
                    // Drinking or snoozing will replace this with the appropriate next alarm.
                    scheduler.scheduleNextAlarm(start, end, interval)

                    val serviceIntent = Intent(context, AlarmService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }

                    // Directly open AlarmActivity on top
                    val activityIntent = Intent(context, AlarmActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    try {
                        context.startActivity(activityIntent)
                    } catch (e: Exception) {
                        Log.e("AlarmReceiver", "Failed to start AlarmActivity directly from receiver", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error processing alarm broadcast", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
