package com.example.hydrate.alarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrate.data.db.HydrateDatabase
import com.example.hydrate.data.model.WaterLog
import com.example.hydrate.data.prefs.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val db = HydrateDatabase.getDatabase(application)
    private val repo = SettingsRepository(application)
    private val scheduler = AlarmScheduler(application)

    val customMessage = repo.selectedMessage.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "Hey love! 💕\nIt's water time 💧"
    )

    fun drinkWater(onComplete: () -> Unit) {
        viewModelScope.launch {
            val amount = repo.glassSizeMl.first()
            
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            db.hydrateDao().insertWaterLog(
                WaterLog(
                    amountMl = amount,
                    timestamp = System.currentTimeMillis(),
                    date = today
                )
            )

            // Schedule the next alarm normally
            val start = repo.startTimeMins.first()
            val end = repo.endTimeMins.first()
            val interval = repo.intervalMins.first()
            scheduler.scheduleNextAlarm(start, end, interval)

            onComplete()
        }
    }

    fun snooze(onComplete: () -> Unit) {
        viewModelScope.launch {
            val snoozeMins = repo.snoozeMins.first()
            scheduler.scheduleSnooze(snoozeMins)
            onComplete()
        }
    }
}
