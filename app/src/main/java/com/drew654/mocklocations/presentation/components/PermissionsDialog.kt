package com.drew654.mocklocations.presentation.components

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun PermissionsDialog(
    showMockLocationDialog: Boolean,
    setShowMockLocationDialog: (Boolean) -> Unit,
    context: Context
) {
    if (showMockLocationDialog) {
        val devOptionsEnabled = try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
            ) != 0
        } catch (e: Exception) {
            false
        }

        AlertDialog(
            onDismissRequest = {},
            title = { Text("Developer Options Required") },
            text = {
                Text(
                    if (devOptionsEnabled)
                        "To use this app, you must select 'Mock Locations' as the Mock Location App in Developer Options."
                    else
                        "You need to enable Developer Options first. Go to Settings > About Phone and tap 'Build Number' 7 times."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            if (devOptionsEnabled) {
                                context.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                            } else {
                                context.startActivity(Intent(Settings.ACTION_DEVICE_INFO_SETTINGS))
                            }
                        } catch (e: Exception) {
                            context.startActivity(Intent(Settings.ACTION_SETTINGS))
                        }
                    }
                ) {
                    Text(if (devOptionsEnabled) "Open Developer Options" else "Open About Phone")
                }
            },
            dismissButton = {
                TextButton(onClick = { setShowMockLocationDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }
}
