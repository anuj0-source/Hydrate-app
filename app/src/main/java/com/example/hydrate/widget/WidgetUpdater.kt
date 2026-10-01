package com.example.hydrate.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WidgetUpdater {
    suspend fun updateAllWidgets(context: Context) {
        try {
            // Widget rendering can involve IPC and data reads; keep it off the UI dispatcher.
            withContext(Dispatchers.Default) {
                HydrateWidget().updateAll(context)
            }
        } catch (e: Exception) {
            Log.e("WidgetUpdater", "Failed to update widget", e)
        }
    }
}
