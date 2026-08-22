package com.wakecalc.alarm.admin

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.wakecalc.alarm.data.Prefs

/**
 * Central helper for the "very hard to delete" behaviour and the deliberate
 * fail-safe escape hatch.
 *
 * How the lock works without root:
 *  - We register as a Device Admin. Android refuses to uninstall an app that
 *    has an active admin, so the normal "uninstall" path is blocked.
 *  - To actually remove the app you must go through the fail-safe below,
 *    which requires solving 3 problems AND waiting out a 60-second cooldown.
 *    Only then do we deactivate admin and launch the uninstaller.
 *
 * Honest limits: without a rooted phone or an ADB "device owner" setup we
 * cannot fully block Settings -> force stop, nor truly disable the hardware
 * power menu. This makes deletion annoying and deliberate, not impossible.
 */
object Lockdown {

    const val FAILSAFE_REQUIRED_SOLVES = 3
    const val FAILSAFE_COOLDOWN_MS = 60_000L

    fun adminComponent(context: Context) =
        ComponentName(context, AppDeviceAdminReceiver::class.java)

    fun isAdminActive(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isAdminActive(adminComponent(context))
    }

    /** Intent that asks the user to grant Device Admin (they must tap Activate). */
    fun enableAdminIntent(context: Context): Intent =
        Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent(context))
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Activate to stop WakeCalc from being uninstalled or skipped. " +
                    "You can still remove it with the in-app fail-safe."
            )
        }

    private fun deactivateAdmin(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (dpm.isAdminActive(adminComponent(context))) {
            runCatching { dpm.removeActiveAdmin(adminComponent(context)) }
        }
    }

    // ---- Fail-safe flow ----------------------------------------------------

    fun startFailsafe(prefs: Prefs) {
        prefs.failsafeStartedAt = System.currentTimeMillis()
        prefs.failsafeSolved = 0
    }

    fun cancelFailsafe(prefs: Prefs) {
        prefs.failsafeStartedAt = 0L
        prefs.failsafeSolved = 0
    }

    fun recordFailsafeSolve(prefs: Prefs) {
        prefs.failsafeSolved = (prefs.failsafeSolved + 1).coerceAtMost(FAILSAFE_REQUIRED_SOLVES)
    }

    fun cooldownRemainingMs(prefs: Prefs): Long {
        if (prefs.failsafeStartedAt == 0L) return FAILSAFE_COOLDOWN_MS
        val elapsed = System.currentTimeMillis() - prefs.failsafeStartedAt
        return (FAILSAFE_COOLDOWN_MS - elapsed).coerceAtLeast(0L)
    }

    fun failsafeReady(prefs: Prefs): Boolean =
        prefs.failsafeStartedAt != 0L &&
            prefs.failsafeSolved >= FAILSAFE_REQUIRED_SOLVES &&
            cooldownRemainingMs(prefs) == 0L

    /** Deactivate admin and launch the system uninstaller for our own package. */
    fun completeUninstall(context: Context, prefs: Prefs) {
        deactivateAdmin(context)
        prefs.alarmEnabled = false
        prefs.lockdownEnabled = false
        cancelFailsafe(prefs)
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:" + context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }
}
