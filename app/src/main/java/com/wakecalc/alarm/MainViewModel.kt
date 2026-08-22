package com.wakecalc.alarm

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wakecalc.alarm.alarm.AlarmScheduler
import com.wakecalc.alarm.challenge.Problem
import com.wakecalc.alarm.data.AppDatabase
import com.wakecalc.alarm.data.Prefs
import com.wakecalc.alarm.data.Stats
import com.wakecalc.alarm.data.WakeStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(app: Application) : AndroidViewModel(app) {

    val prefs = Prefs(app)

    val stats: StateFlow<Stats> =
        AppDatabase.get(app).wakeDao().observeAll()
            .map { WakeStats.compute(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                WakeStats.compute(emptyList())
            )

    // Mirror settings into Compose state so the UI recomposes on change.
    var alarmEnabled by mutableStateOf(prefs.alarmEnabled); private set
    var hour by mutableStateOf(prefs.hour); private set
    var minute by mutableStateOf(prefs.minute); private set
    var days by mutableStateOf(prefs.days); private set
    var soundName by mutableStateOf(prefs.soundName); private set
    var categories by mutableStateOf(prefs.categories); private set
    var difficulty by mutableStateOf(prefs.difficulty); private set

    private fun resync() = AlarmScheduler.reschedule(getApplication())

    fun setEnabled(v: Boolean) { prefs.alarmEnabled = v; alarmEnabled = v; resync() }

    fun setTime(h: Int, m: Int) {
        prefs.hour = h; prefs.minute = m; hour = h; minute = m; resync()
    }

    fun toggleDay(day: Int) {
        val d = days.toMutableSet()
        if (!d.add(day)) d.remove(day)
        prefs.days = d; days = d; resync()
    }

    fun setSound(uri: String?, name: String) {
        prefs.soundUri = uri; prefs.soundName = name; soundName = name
    }

    fun toggleCategory(c: Problem.Category) {
        val s = categories.toMutableSet()
        if (!s.add(c)) s.remove(c)
        if (s.isEmpty()) return // keep at least one
        prefs.categories = s; categories = s
    }

    fun updateDifficulty(v: Int) { prefs.difficulty = v; difficulty = v }
}
