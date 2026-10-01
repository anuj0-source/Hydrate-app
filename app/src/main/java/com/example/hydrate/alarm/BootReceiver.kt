package com.example.hydrate.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.hydrate.data.prefs.SettingsRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed. Rescheduling alarms...")
            val repo = SettingsRepository(context)
            val scheduler = AlarmScheduler(context)

            GlobalScope.launch(Dispatchers.IO) {
                val enabled = repo.isAlarmEnabled.first()
                if (enabled) {
                    val start = repo.startTimeMins.first()
                    val end = repo.endTimeMins.first()
                    val interval = repo.intervalMins.first()
                    scheduler.scheduleNextAlarm(start, end, interval)
                }
            }
        }
    }
}
