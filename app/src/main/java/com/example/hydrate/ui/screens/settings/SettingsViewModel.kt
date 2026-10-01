package com.example.hydrate.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrate.alarm.AlarmScheduler
import com.example.hydrate.data.prefs.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = SettingsRepository(application)
    private val scheduler = AlarmScheduler(application)

    val isAlarmEnabled = repo.isAlarmEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val startTimeMins = repo.startTimeMins.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 480)
    val endTimeMins = repo.endTimeMins.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1320)
    val intervalMins = repo.intervalMins.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 60)
    val dailyGoalMl = repo.dailyGoalMl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2000)
    val glassSizeMl = repo.glassSizeMl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 250)

    fun setAlarmEnabled(enabled: Boolean) = viewModelScope.launch { 
        repo.setAlarmEnabled(enabled) 
        if (enabled) reschedule() else scheduler.cancelAlarm()
    }

    fun setStartTime(mins: Int) = viewModelScope.launch { 
        repo.setStartTimeMins(mins)
        reschedule()
    }
    
    fun setEndTime(mins: Int) = viewModelScope.launch { 
        repo.setEndTimeMins(mins)
        reschedule()
    }

    fun setInterval(mins: Int) = viewModelScope.launch { 
        repo.setIntervalMins(mins)
        reschedule()
    }

    fun setDailyGoal(ml: Int) = viewModelScope.launch {
        repo.setDailyGoalMl(ml)
        reschedule()
    }

    fun setGlassSize(ml: Int) = viewModelScope.launch {
        repo.setGlassSizeMl(ml)
        reschedule()
    }

    private fun reschedule() = viewModelScope.launch {
        if (isAlarmEnabled.value) {
            scheduler.scheduleNextAlarm(startTimeMins.value, endTimeMins.value, intervalMins.value)
        }
    }

    fun testAlarmIn10Seconds() {
        scheduler.scheduleTestAlarm(10)
    }
    
    fun testAlarmNow() {
        scheduler.scheduleTestAlarm(1) // 1 second
    }
}
