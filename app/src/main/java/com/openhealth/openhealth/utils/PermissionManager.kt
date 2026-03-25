package com.openhealth.openhealth.utils

import android.content.Context
import android.content.SharedPreferences

object PermissionManager {
    private const val PREFS_NAME = "health_permissions"
    private const val KEY_PERMISSIONS_GRANTED = "permissions_granted"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun hasPermissionsBeenGranted(): Boolean {
        return prefs.getBoolean(KEY_PERMISSIONS_GRANTED, false)
    }

    fun setPermissionsGranted(granted: Boolean) {
        prefs.edit().putBoolean(KEY_PERMISSIONS_GRANTED, granted).apply()
    }

    fun clearPermissionState() {
        prefs.edit().remove(KEY_PERMISSIONS_GRANTED).apply()
    }
}
