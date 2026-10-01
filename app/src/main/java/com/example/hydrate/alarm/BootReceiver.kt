package com.example.hydrate.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.hydrate.data.prefs.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.d("BootReceiver", "Boot completed. Rescheduling alarms...")
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = SettingsRepository(context)
                val enabled = repo.isAlarmEnabled.first()
                if (enabled) {
                    val start = repo.startTimeMins.first()
                    val end = repo.endTimeMins.first()
                    val interval = repo.intervalMins.first()
                    AlarmScheduler(context).scheduleNextAlarm(start, end, interval)
                }
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to reschedule alarms after boot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
