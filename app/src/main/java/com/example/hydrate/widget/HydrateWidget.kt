package com.example.hydrate.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.hydrate.alarm.AlarmScheduler
import com.example.hydrate.data.db.HydrateDatabase
import com.example.hydrate.data.model.WaterLog
import com.example.hydrate.data.prefs.SettingsRepository
import com.example.hydrate.data.time.LocalDay
import kotlinx.coroutines.flow.first

// Exact Colors from Reference Image
private val RefBlushBg = Color(0xFFFFE6EE)        // Soft kawaii blush pink card background
private val RefDarkPlum = Color(0xFF381622)       // Rich Dark Plum for "Hydrate" & "6 / 8"

class HydrateWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = SettingsRepository(context)
        val db = HydrateDatabase.getDatabase(context)

        val today = LocalDay.keyAt()
        val dao = db.hydrateDao()
        val initialTotalWater = dao.getTotalWaterForDate(today).first() ?: 0
        val initialDrinkCount = dao.getDrinkCountForDate(today).first()
        val initialGlassSize = repo.glassSizeMl.first()
        val initialDailyGoal = repo.dailyGoalMl.first()

        provideContent {
            // update() does not restart an already-running Glance composition. Observing the
            // database and settings here lets the count recompose as soon as a widget tap
            // inserts its new WaterLog.
            val totalWaterOrNull by dao.getTotalWaterForDate(today).collectAsState(initialTotalWater)
            val totalWater = totalWaterOrNull ?: 0
            val drinkCount by dao.getDrinkCountForDate(today).collectAsState(initialDrinkCount)
            val glassSize by repo.glassSizeMl.collectAsState(initialGlassSize)
            val dailyGoal by repo.dailyGoalMl.collectAsState(initialDailyGoal)
            val targetGlasses = if (glassSize > 0) {
                (dailyGoal + glassSize - 1) / glassSize
            } else {
                0
            }
            val isCompleted = totalWater >= dailyGoal && dailyGoal > 0

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .background(ColorProvider(day = RefBlushBg, night = RefBlushBg))
                    .cornerRadius(32.dp)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Title
                    Text(
                        text = "Hydrate 💕",
                        style = TextStyle(
                            color = ColorProvider(day = RefDarkPlum, night = RefDarkPlum),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(GlanceModifier.height(12.dp))

                    // Water Progress Counter (💧 6 / 8)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💧 ",
                            style = TextStyle(fontSize = 24.sp)
                        )
                        Text(
                            text = "$drinkCount / $targetGlasses",
                            style = TextStyle(
                                color = ColorProvider(day = RefDarkPlum, night = RefDarkPlum),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(GlanceModifier.height(16.dp))

                    // Use Glance's native Button so the host receives a proper click action.
                    Button(
                        text = if (isCompleted) "All hydrated! 💕" else "Drink Water 💧",
                        onClick = actionRunCallback<DrinkWaterAction>(),
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .height(48.dp)
                    )
                }
            }
        }
    }
}

class HydrateWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HydrateWidget()
}

class DrinkWaterAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val repo = SettingsRepository(context)
        val db = HydrateDatabase.getDatabase(context)
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
        AlarmScheduler(context).scheduleNextAlarm(
            start,
            end,
            interval,
            skipNextScheduledReminder = true
        )

        // Refresh every placed widget. Active compositions also observe the inserted row above.
        WidgetUpdater.updateAllWidgets(context)
    }
}
