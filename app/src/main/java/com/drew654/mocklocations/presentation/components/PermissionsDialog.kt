package com.drew654.mocklocations.presentation.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.drew654.mocklocations.domain.model.Permission

@Composable
fun PermissionsDialog(
    permission: Permission,
    setShowMockLocationDialog: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    context: Context
) {
    val (bodyText, buttonText, titleText) = when (permission) {
        is Permission.FineLocation -> Triple(
            "To use this app, you must grant \"Fine Location\" permission in App Settings.",
            "Open Location Settings",
            "Location Permission Required"
        )

        is Permission.MockLocations -> Triple(
            "To use this app, you must select \"Mock Locations\" as the Mock Location App in Developer Options. It should be near the bottom of the list.",
            "Open Developer Options",
            "Developer Options Required"
        )

        is Permission.DeveloperOptions -> Triple(
            "You need to enable Developer Options first. Go to Settings > About phone and tap \"Build Number\" 7 times.",
            "Open About Phone",
            "Developer Options Required"
        )

        else -> return
    }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = { Text(titleText) },
        text = {
            Text(bodyText)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        when (permission) {
                            is Permission.FineLocation -> {
                                context.startActivity(
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts(
                                            "package",
                                            context.packageName,
                                            null
                                        )
                                    }
                                )
                            }

                            is Permission.MockLocations -> context.startActivity(
                                Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                            )

                            is Permission.DeveloperOptions -> context.startActivity(
                                Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)
                            )

                            is Permission.PostNotifications -> return@TextButton
                        }
                    } catch (_: Exception) {
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
