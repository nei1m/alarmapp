package com.wakecalc.alarm.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.wakecalc.alarm.data.AppDatabase
import com.wakecalc.alarm.data.Stats
import com.wakecalc.alarm.data.WakeStats

object WidgetUpdater {
    suspend fun loadStats(context: Context): Stats {
        val logs = AppDatabase.get(context).wakeDao().getAll()
        return WakeStats.compute(logs)
    }

    suspend fun updateAll(context: Context) {
        val manager = GlanceAppWidgetManager(context)

        val streak = StreakWidget()
        manager.getGlanceIds(StreakWidget::class.java).forEach { id ->
            streak.update(context, id)
        }

        val tracker = TrackerWidget()
        manager.getGlanceIds(TrackerWidget::class.java).forEach { id ->
            tracker.update(context, id)
        }
    }
}
