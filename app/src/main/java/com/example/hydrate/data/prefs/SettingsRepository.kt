package com.example.hydrate.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("hydrate_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val ALARM_ENABLED = booleanPreferencesKey("alarm_enabled")
        val START_TIME_MINS = intPreferencesKey("start_time_mins") // minutes from midnight (e.g., 8:00 AM = 480)
        val END_TIME_MINS = intPreferencesKey("end_time_mins")     // e.g., 10:00 PM = 1320
        val INTERVAL_MINS = intPreferencesKey("interval_mins")     // e.g., 60
        val DAILY_GOAL_ML = intPreferencesKey("daily_goal_ml")
        val GLASS_SIZE_ML = intPreferencesKey("glass_size_ml")
        val SELECTED_MESSAGE = stringPreferencesKey("selected_message")
        val SNOOZE_MINS = intPreferencesKey("snooze_mins")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBE_ENABLED = booleanPreferencesKey("vibe_enabled")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val isAlarmEnabled: Flow<Boolean> = context.dataStore.data.map { it[ALARM_ENABLED] ?: false }
    val startTimeMins: Flow<Int> = context.dataStore.data.map { it[START_TIME_MINS] ?: 480 }
    val endTimeMins: Flow<Int> = context.dataStore.data.map { it[END_TIME_MINS] ?: 1320 }
    val intervalMins: Flow<Int> = context.dataStore.data.map { it[INTERVAL_MINS] ?: 60 }
    val dailyGoalMl: Flow<Int> = context.dataStore.data.map { it[DAILY_GOAL_ML] ?: 2000 }
    val glassSizeMl: Flow<Int> = context.dataStore.data.map { it[GLASS_SIZE_ML] ?: 250 }
    val selectedMessage: Flow<String> = context.dataStore.data.map { it[SELECTED_MESSAGE] ?: "Hey love! 💕\nIt's water time 💧" }
    val snoozeMins: Flow<Int> = context.dataStore.data.map { it[SNOOZE_MINS] ?: 5 }
    val isSoundEnabled: Flow<Boolean> = context.dataStore.data.map { it[SOUND_ENABLED] ?: true }
    val isVibeEnabled: Flow<Boolean> = context.dataStore.data.map { it[VIBE_ENABLED] ?: true }
    val isOnboardingDone: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_DONE] ?: false }

    suspend fun setAlarmEnabled(enabled: Boolean) = context.dataStore.edit { it[ALARM_ENABLED] = enabled }
    suspend fun setStartTimeMins(mins: Int) = context.dataStore.edit { it[START_TIME_MINS] = mins }
    suspend fun setEndTimeMins(mins: Int) = context.dataStore.edit { it[END_TIME_MINS] = mins }
    suspend fun setIntervalMins(mins: Int) = context.dataStore.edit { it[INTERVAL_MINS] = mins }
    suspend fun setDailyGoalMl(ml: Int) = context.dataStore.edit { it[DAILY_GOAL_ML] = ml }
    suspend fun setGlassSizeMl(ml: Int) = context.dataStore.edit { it[GLASS_SIZE_ML] = ml }
    suspend fun setSelectedMessage(msg: String) = context.dataStore.edit { it[SELECTED_MESSAGE] = msg }
    suspend fun setSnoozeMins(mins: Int) = context.dataStore.edit { it[SNOOZE_MINS] = mins }
    suspend fun setSoundEnabled(enabled: Boolean) = context.dataStore.edit { it[SOUND_ENABLED] = enabled }
    suspend fun setVibeEnabled(enabled: Boolean) = context.dataStore.edit { it[VIBE_ENABLED] = enabled }
    suspend fun setOnboardingDone(done: Boolean) = context.dataStore.edit { it[ONBOARDING_DONE] = done }
}
