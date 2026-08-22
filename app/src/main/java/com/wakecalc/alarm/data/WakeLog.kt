package com.wakecalc.alarm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One logged wake-up: the day, the exact time you dismissed the alarm by
 * solving, how long the problem took, and which problem it was.
 */
@Entity(tableName = "wake_log")
data class WakeLog(
    @PrimaryKey val dayEpoch: Long,      // days since epoch (one entry per day)
    val wakeTimeMillis: Long,            // absolute time you solved it
    val minutesOfDay: Int,               // 0..1439, for averaging wake time
    val solveMillis: Long,               // how long you took to solve
    val category: String,                // problem category label
    val prompt: String                   // the problem you solved
)
