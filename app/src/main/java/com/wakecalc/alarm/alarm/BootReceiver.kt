package com.wakecalc.alarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Reschedules the alarm after a reboot or app update. This is the
 * "reboot persistence" part of lockdown: turning the phone off and on
 * does not clear the alarm.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED ->
                AlarmScheduler.reschedule(context)
        }
    }
}
