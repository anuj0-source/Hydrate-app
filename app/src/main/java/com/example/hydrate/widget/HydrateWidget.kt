package com.example.hydrate.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.hydrate.alarm.AlarmScheduler
import com.example.hydrate.data.db.HydrateDatabase
import com.example.hydrate.data.model.WaterLog
import com.example.hydrate.data.prefs.SettingsRepository
import com.example.hydrate.ui.theme.PastelBlushPink
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HydrateWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Fetch data
        val repo = SettingsRepository(context)
        val db = HydrateDatabase.getDatabase(context)
        
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val totalWater = db.hydrateDao().getTotalWaterForDate(today).first() ?: 0
        val glassSize = repo.glassSizeMl.first()
        val dailyGoal = repo.dailyGoalMl.first()
        
        val glasses = if (glassSize > 0) totalWater / glassSize else 0
        val targetGlasses = if (glassSize > 0) dailyGoal / glassSize else 0

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(day = PastelBlushPink, night = PastelBlushPink))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Hydrate 💕",
                    style = TextStyle(fontWeight = FontWeight.Bold)
                )
                Spacer(GlanceModifier.height(8.dp))
                Text(text = "💧 $glasses / $targetGlasses")
                Spacer(GlanceModifier.height(16.dp))
                Button(
                    text = "Drink Water 💧",
                    onClick = actionRunCallback<DrinkWaterAction>()
                )
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
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        db.hydrateDao().insertWaterLog(
            WaterLog(
                amountMl = amount,
                timestamp = System.currentTimeMillis(),
                date = today
            )
        )
        val start = repo.startTimeMins.first()
        val end = repo.endTimeMins.first()
        val interval = repo.intervalMins.first()
        AlarmScheduler(context).scheduleNextAlarm(start, end, interval)

        // Refresh widget
        HydrateWidget().update(context, glanceId)
    }
}
