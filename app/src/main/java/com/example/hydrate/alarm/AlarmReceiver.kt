package com.example.hydrate.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.hydrate.data.db.HydrateDatabase
import com.example.hydrate.data.prefs.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = HydrateDatabase.getDatabase(context)
                val repo = SettingsRepository(context)
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                val totalWater = db.hydrateDao().getTotalWaterForDate(today).first() ?: 0
                val dailyGoal = repo.dailyGoalMl.first()

                val start = repo.startTimeMins.first()
                val end = repo.endTimeMins.first()
                val interval = repo.intervalMins.first()
                val scheduler = AlarmScheduler(context)

                if (totalWater >= dailyGoal) {
                    Log.d("AlarmReceiver", "Daily goal already completed ($totalWater/$dailyGoal ml). Skipping alarm and rescheduling for tomorrow.")
                    scheduler.scheduleNextAlarm(start, end, interval)
                } else {
                    Log.d("AlarmReceiver", "Daily goal not met ($totalWater/$dailyGoal ml). Triggering alarm.")
                    val serviceIntent = Intent(context, AlarmService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
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
