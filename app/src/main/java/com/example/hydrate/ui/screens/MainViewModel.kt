package com.example.hydrate.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrate.alarm.AlarmScheduler
import com.example.hydrate.data.db.HydrateDatabase
import com.example.hydrate.data.model.WaterLog
import com.example.hydrate.data.prefs.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = HydrateDatabase.getDatabase(application)
    private val repo = SettingsRepository(application)
    
    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val totalWaterToday = db.hydrateDao().getTotalWaterForDate(today)
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        
    val todayLogs = db.hydrateDao().getWaterLogsForDate(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyGoalMl = repo.dailyGoalMl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2000)
    val glassSizeMl = repo.glassSizeMl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 250)

    val progressPercent = combine(totalWaterToday, dailyGoalMl) { current, goal ->
        if (goal > 0) (current.toFloat() / goal.toFloat()).coerceAtMost(1f) else 0f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val glassesToday = combine(totalWaterToday, glassSizeMl) { current, size ->
        if (size > 0) current / size else 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val targetGlasses = combine(dailyGoalMl, glassSizeMl) { goal, size ->
        if (size > 0) goal / size else 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun drinkWater() {
        viewModelScope.launch {
            val amount = repo.glassSizeMl.first()
            val now = System.currentTimeMillis()
            db.hydrateDao().insertWaterLog(
                WaterLog(
                    amountMl = amount,
                    timestamp = now,
                    date = today
                )
            )
            val start = repo.startTimeMins.first()
            val end = repo.endTimeMins.first()
            val interval = repo.intervalMins.first()
            val scheduler = AlarmScheduler(getApplication())
            scheduler.scheduleNextAlarm(start, end, interval)
        }
    }
}
