package com.wakecalc.alarm

import android.app.Application
import com.wakecalc.alarm.alarm.AlarmScheduler

class WakeCalcApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Make sure a scheduled alarm exists if one is enabled (e.g. after update).
        AlarmScheduler.reschedule(this)
    }
}
