package com.wakecalc.alarm.data

import java.util.concurrent.TimeUnit

/** Derived, display-ready stats computed from the raw wake log. */
data class WeeklyBar(val dayLabel: String, val solved: Boolean, val minutesOfDay: Int)

data class Stats(
    val streak: Int,
    val totalSolved: Int,
    val avgWakeMinutes: Int?,     // null if no data
    val bestWakeMinutes: Int?,
    val week: List<WeeklyBar>,    // 7 entries, Monday..Sunday of current week
    val todaySolved: Boolean,
    val todayWakeMinutes: Int?
)

object WakeStats {

    fun todayEpochDay(nowMillis: Long = System.currentTimeMillis()): Long =
        TimeUnit.MILLISECONDS.toDays(nowMillis + localOffsetMillis())

    private fun localOffsetMillis(): Long =
        java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis()).toLong()

    fun formatMinutes(m: Int?): String {
        if (m == null) return "—"
        val h = m / 60
        val min = m % 60
        val ampm = if (h < 12) "AM" else "PM"
        val h12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        return "%d:%02d %s".format(h12, min, ampm)
    }

    fun compute(logs: List<WakeLog>, nowMillis: Long = System.currentTimeMillis()): Stats {
        val today = todayEpochDay(nowMillis)
        val byDay = logs.associateBy { it.dayEpoch }

        // streak: consecutive days ending today or yesterday
        var streak = 0
        var cursor = today
        if (byDay.containsKey(today) || byDay.containsKey(today - 1)) {
            if (!byDay.containsKey(today)) cursor = today - 1
            while (byDay.containsKey(cursor)) {
                streak++
                cursor--
            }
        }

        val solvedMinutes = logs.map { it.minutesOfDay }
        val avg = if (solvedMinutes.isEmpty()) null else solvedMinutes.average().toInt()
        val best = solvedMinutes.minOrNull()

        // week: Monday..Sunday containing today
        val dow = ((today % 7) + 7) % 7          // 0=Thu at epoch; normalise below
        // Compute Monday of this week. Epoch day 0 = 1970-01-01 = Thursday.
        // dayOfWeek where Monday=0: (epochDay + 3) % 7
        val mondayOffset = ((today + 3) % 7 + 7) % 7
        val monday = today - mondayOffset
        val labels = listOf("M", "T", "W", "T", "F", "S", "S")
        val week = (0..6).map { i ->
            val d = monday + i
            val log = byDay[d]
            WeeklyBar(labels[i], log != null, log?.minutesOfDay ?: -1)
        }

        return Stats(
            streak = streak,
            totalSolved = logs.size,
            avgWakeMinutes = avg,
            bestWakeMinutes = best,
            week = week,
            todaySolved = byDay.containsKey(today),
            todayWakeMinutes = byDay[today]?.minutesOfDay
        )
    }
}
