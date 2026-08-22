package com.wakecalc.alarm.data

import android.content.Context
import com.wakecalc.alarm.challenge.Problem

/**
 * Lightweight settings store. Kept synchronous (SharedPreferences) so alarm
 * receivers, the foreground service and home-screen widgets can all read it
 * without coroutines.
 */
class Prefs(context: Context) {
    private val sp = context.applicationContext.getSharedPreferences("wakecalc", Context.MODE_PRIVATE)

    var alarmEnabled: Boolean
        get() = sp.getBoolean("alarm_enabled", false)
        set(v) = sp.edit().putBoolean("alarm_enabled", v).apply()

    var hour: Int
        get() = sp.getInt("hour", 6)
        set(v) = sp.edit().putInt("hour", v).apply()

    var minute: Int
        get() = sp.getInt("minute", 30)
        set(v) = sp.edit().putInt("minute", v).apply()

    /** Repeat days as a set of Calendar day-of-week ints (1=Sun..7=Sat). */
    var days: Set<Int>
        get() = sp.getStringSet("days", setOf("2", "3", "4", "5", "6"))!!
            .map { it.toInt() }.toSet()
        set(v) = sp.edit().putStringSet("days", v.map { it.toString() }.toSet()).apply()

    /** content:// URI of the chosen MP3, or null for the default alarm tone. */
    var soundUri: String?
        get() = sp.getString("sound_uri", null)
        set(v) = sp.edit().putString("sound_uri", v).apply()

    var soundName: String
        get() = sp.getString("sound_name", "Default alarm tone")!!
        set(v) = sp.edit().putString("sound_name", v).apply()

    var categories: Set<Problem.Category>
        get() {
            val raw = sp.getStringSet(
                "categories",
                Problem.Category.values().map { it.name }.toSet()
            )!!
            return raw.mapNotNull { runCatching { Problem.Category.valueOf(it) }.getOrNull() }.toSet()
                .ifEmpty { Problem.Category.values().toSet() }
        }
        set(v) = sp.edit().putStringSet("categories", v.map { it.name }.toSet()).apply()

    var difficulty: Int
        get() = sp.getInt("difficulty", 1)
        set(v) = sp.edit().putInt("difficulty", v.coerceIn(0, 3)).apply()

    var lockdownEnabled: Boolean
        get() = sp.getBoolean("lockdown", false)
        set(v) = sp.edit().putBoolean("lockdown", v).apply()

    /** Timestamp when the fail-safe uninstall cooldown was started (0 = idle). */
    var failsafeStartedAt: Long
        get() = sp.getLong("failsafe_started", 0L)
        set(v) = sp.edit().putLong("failsafe_started", v).apply()

    var failsafeSolved: Int
        get() = sp.getInt("failsafe_solved", 0)
        set(v) = sp.edit().putInt("failsafe_solved", v).apply()
}
