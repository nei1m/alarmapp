package com.wakecalc.alarm.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

/**
 * Being an active Device Admin is what blocks a normal uninstall: Android
 * won't let you uninstall an app while it holds an active admin component.
 * The in-app fail-safe deactivates this first, then uninstalls.
 */
class AppDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence =
        "Turning this off removes WakeCalc's uninstall protection. Use the in-app fail-safe if you're locked out."
}
