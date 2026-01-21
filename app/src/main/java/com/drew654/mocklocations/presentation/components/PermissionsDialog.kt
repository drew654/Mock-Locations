package com.drew654.mocklocations.presentation.components

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.drew654.mocklocations.presentation.hasFineLocationPermission
import com.drew654.mocklocations.presentation.isDeveloperOptionsEnabled

@Composable
fun PermissionsDialog(
    showMockLocationDialog: Boolean,
    setShowMockLocationDialog: (Boolean) -> Unit,
    context: Context
) {
    if (showMockLocationDialog) {
        val (bodyText, buttonText) = when (true) {
            !hasFineLocationPermission(context) -> Pair(
                "To use this app, you must grant 'Fine Location' permission in App Settings.",
                "Open Location Settings"
            )

            isDeveloperOptionsEnabled(context) -> Pair(
                "To use this app, you must select 'Mock Locations' as the Mock Location App in Developer Options.",
                "Open Developer Options"
            )

            else -> Pair(
                "You need to enable Developer Options first. Go to Settings > About Phone and tap 'Build Number' 7 times.",
                "Open About Phone"
            )
        }

        AlertDialog(
            onDismissRequest = { },
            title = { Text(if (!hasFineLocationPermission(context)) "Location Permission Required" else "Developer Options Required") },
            text = {
                Text(bodyText)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            when (true) {
                                !hasFineLocationPermission(context) -> {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = android.net.Uri.fromParts(
                                                "package",
                                                context.packageName,
                                                null
                                            )
                                        }
                                    )
                                }

                                isDeveloperOptionsEnabled(context) -> context.startActivity(
                                    Intent(
                                        Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
                                    )
                                )

                                else -> context.startActivity(Intent(Settings.ACTION_DEVICE_INFO_SETTINGS))
                            }
                        } catch (e: Exception) {
                            context.startActivity(Intent(Settings.ACTION_SETTINGS))
                        }
                    }
                ) {
                    Text(buttonText)
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
