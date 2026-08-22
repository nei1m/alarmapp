package com.wakecalc.alarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.wakecalc.alarm.data.Prefs
import java.util.Calendar

/**
 * Schedules the next alarm using an exact, wake-the-device AlarmManager
 * alarm. Uses setAlarmClock so it survives Doze and is treated as a
 * user-visible alarm by the OS.
 */
object AlarmScheduler {

    const val REQUEST_CODE = 4201

    fun reschedule(context: Context) {
        val prefs = Prefs(context)
        cancel(context)
        if (!prefs.alarmEnabled) return
        val next = nextTriggerMillis(prefs) ?: return

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val show = PendingIntent.getActivity(
            context, 9001,
            Intent(context, AlarmActivity::class.java),
            pendingFlags()
        )
        val fire = alarmPendingIntent(context)
        val info = AlarmManager.AlarmClockInfo(next, show)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            // Fall back to inexact if the user hasn't granted exact-alarm permission.
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, fire)
        } else {
            am.setAlarmClock(info, fire)
        }
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(alarmPendingIntent(context))
    }

    private fun alarmPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM
        }
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, pendingFlags())
    }

    private fun pendingFlags(): Int =
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    /** Next time (ms) matching the enabled days, or a one-shot if no days set. */
    fun nextTriggerMillis(prefs: Prefs, from: Long = System.currentTimeMillis()): Long? {
        val days = prefs.days
        val base = Calendar.getInstance().apply {
            timeInMillis = from
            set(Calendar.HOUR_OF_DAY, prefs.hour)
            set(Calendar.MINUTE, prefs.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (days.isEmpty()) {
            if (base.timeInMillis <= from) base.add(Calendar.DAY_OF_YEAR, 1)
            return base.timeInMillis
        }
        for (i in 0..7) {
            val c = base.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, i)
            if (c.timeInMillis <= from) continue
            if (days.contains(c.get(Calendar.DAY_OF_WEEK))) return c.timeInMillis
        }
        return null
    }
}
