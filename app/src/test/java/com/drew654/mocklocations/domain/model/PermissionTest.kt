package com.drew654.mocklocations.domain.model

import android.Manifest
import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAppOpsManager

@RunWith(RobolectricTestRunner::class)
class PermissionTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `FineLocation returns true when permission granted`() {
        val shadowApp = Shadows.shadowOf(context as Application)
        shadowApp.grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        val result = Permission.FineLocation.isGranted(context)
        assertTrue(result)
    }

    @Test
    fun `FineLocation returns false when permission is not granted`() {
        val result = Permission.FineLocation.isGranted(context)
        assertFalse(result)
    }

    @Test
    fun `DeveloperOptions returns true when enabled`() {
        Settings.Global.putInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1)

        val result = Permission.DeveloperOptions.isGranted(context)
        assertTrue(result)
    }

    @Test
    fun `DeveloperOptions returns false when disabled`() {
        Settings.Global.putInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)

        val result = Permission.DeveloperOptions.isGranted(context)
        assertFalse(result)
    }

    @Test
    fun `MockLocations returns true when app is set as mock provider`() {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val shadowAppOps: ShadowAppOpsManager = Shadows.shadowOf(appOpsManager)

        shadowAppOps.setMode(
            AppOpsManager.OPSTR_MOCK_LOCATION,
            Process.myUid(),
            context.packageName,
            AppOpsManager.MODE_ALLOWED
        )

        val result = Permission.MockLocations.isGranted(context)
        assertTrue(result)
    }

    @Test
    fun `MockLocations returns false when app is not set as mock provider`() {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val shadowAppOps: ShadowAppOpsManager = Shadows.shadowOf(appOpsManager)

        shadowAppOps.setMode(
            AppOpsManager.OPSTR_MOCK_LOCATION,
            Process.myUid(),
            context.packageName,
            AppOpsManager.MODE_ERRORED
        )

        val result = Permission.MockLocations.isGranted(context)
        assertFalse(result)
    }

    @Test
    fun `PostNotifications returns true when permission granted`() {
        val shadowApp = Shadows.shadowOf(context as Application)
        shadowApp.grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        val result = Permission.PostNotifications.isGranted(context)
        assertTrue(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `PostNotifications returns false when permission is not granted for Android 13`() {
        val result = Permission.PostNotifications.isGranted(context)
        assertFalse(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `PostNotifications returns true below Android 13`() {
        val result = Permission.PostNotifications.isGranted(context)
        assertTrue(result)
    }
}
