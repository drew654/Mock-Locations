package com.drew654.mocklocations.domain.model

import android.Manifest
import android.app.AppOpsManager
import android.provider.Settings

sealed class Permission(val permission: String) {
    object FineLocation : Permission(Manifest.permission.ACCESS_FINE_LOCATION)

    object DeveloperOptions : Permission(Settings.Global.DEVELOPMENT_SETTINGS_ENABLED)

    object MockLocations : Permission(AppOpsManager.OPSTR_MOCK_LOCATION)
}
