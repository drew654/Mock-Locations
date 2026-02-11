package com.drew654.mocklocations.domain.model

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Context.APP_OPS_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

sealed class Permission(val permission: String) {
    object FineLocation : Permission(Manifest.permission.ACCESS_FINE_LOCATION)

    object DeveloperOptions : Permission(Settings.Global.DEVELOPMENT_SETTINGS_ENABLED)

    object MockLocations : Permission(AppOpsManager.OPSTR_MOCK_LOCATION)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    object PostNotifications : Permission(Manifest.permission.POST_NOTIFICATIONS)
}

fun Permission.isGranted(context: Context): Boolean {
    return when (this) {
        is Permission.FineLocation -> hasFineLocationPermission(context)
        is Permission.DeveloperOptions -> isDeveloperOptionsEnabled(context)
        is Permission.MockLocations -> isAppSetAsMockLocationsApp(context)
        is Permission.PostNotifications -> hasNotificationPermission(context)
    }
}

private fun hasFineLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

private fun isDeveloperOptionsEnabled(context: Context): Boolean {
    return try {
        Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
        ) != 0
    } catch (_: Exception) {
        false
    }
}

private fun hasNotificationPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}

private fun isAppSetAsMockLocationsApp(context: Context): Boolean {
    try {
        val opsManager = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = opsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_MOCK_LOCATION,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    } catch (_: Exception) {
        return false
    }
}
