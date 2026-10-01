package com.example.hydrate.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrate.alarm.AlarmScheduler
import com.example.hydrate.data.db.HydrateDatabase
import com.example.hydrate.data.model.WaterLog
import com.example.hydrate.data.prefs.SettingsRepository
import com.example.hydrate.data.time.LocalDay
import com.example.hydrate.widget.WidgetUpdater
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class HomeUiState(
    val totalWaterMl: Int = 0,
    val progressPercent: Float = 0f,
    val glasses: Int = 0,
    val targetGlasses: Int = 0,
    val logs: List<WaterLog> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = HydrateDatabase.getDatabase(application)
    private val repo = SettingsRepository(application)

    private val currentDay = flow {
        while (true) {
            val now = System.currentTimeMillis()
            emit(LocalDay.keyAt(now))
            delay(LocalDay.millisUntilNextMidnight(now))
        }
    }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, LocalDay.keyAt())

    val totalWaterToday = currentDay
        .flatMapLatest { date -> db.hydrateDao().getTotalWaterForDate(date) }
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayLogs = currentDay
        .flatMapLatest { date -> db.hydrateDao().getWaterLogsForDate(date) }
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
        if (size > 0) (goal + size - 1) / size else 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val homeUiState = combine(
        totalWaterToday,
        todayLogs,
        dailyGoalMl,
        glassSizeMl
    ) { totalWater, logs, goal, glassSize ->
        HomeUiState(
            totalWaterMl = totalWater,
            progressPercent = if (goal > 0) {
                (totalWater.toFloat() / goal).coerceAtMost(1f)
            } else {
                0f
            },
            glasses = if (glassSize > 0) totalWater / glassSize else 0,
            targetGlasses = if (glassSize > 0) {
                (goal + glassSize - 1) / glassSize
            } else {
                0
            },
            logs = logs
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun drinkWater() {
        viewModelScope.launch {
            val amount = repo.glassSizeMl.first()
            val timestamp = System.currentTimeMillis()
            db.hydrateDao().insertWaterLog(
                WaterLog(
                    amountMl = amount,
                    timestamp = timestamp,
                    date = LocalDay.keyAt(timestamp)
                )
            )
            val start = repo.startTimeMins.first()
            val end = repo.endTimeMins.first()
            val interval = repo.intervalMins.first()
            val scheduler = AlarmScheduler(getApplication())
            scheduler.scheduleNextAlarm(
                start,
                end,
                interval,
                skipNextScheduledReminder = true
            )

            WidgetUpdater.updateAllWidgets(getApplication())
        }
    }
}
