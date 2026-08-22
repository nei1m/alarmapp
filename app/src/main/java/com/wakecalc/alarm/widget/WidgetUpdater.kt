package com.wakecalc.alarm.widget

import android.content.Context
import com.wakecalc.alarm.data.AppDatabase
import com.wakecalc.alarm.data.Stats
import com.wakecalc.alarm.data.WakeStats

object WidgetUpdater {
    suspend fun loadStats(context: Context): Stats {
        val logs = AppDatabase.get(context).wakeDao().getAll()
        return WakeStats.compute(logs)
    }

    suspend fun updateAll(context: Context) {
        StreakWidget().updateAll(context)
        TrackerWidget().updateAll(context)
    }
}
