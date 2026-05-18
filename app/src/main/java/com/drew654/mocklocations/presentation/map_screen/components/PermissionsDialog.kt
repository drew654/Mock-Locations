package com.drew654.mocklocations.presentation.map_screen.components

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.drew654.mocklocations.domain.model.Permission
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

@Composable
fun PermissionsDialog(
    permission: Permission,
    onDismiss: () -> Unit = { }
) {
    val context = LocalContext.current
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
            "You need to enable Developer Options first. Go to Settings > About Phone and tap \"Build Number\" 7 times. \"Build Number\" may be found in About Phone > Software Information on Samsung devices.",
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
                    } catch (e: Exception) {
                        Log.e("PermissionsDialog", "Failed to open system settings", e)
                        context.startActivity(Intent(Settings.ACTION_SETTINGS))
                    }
                }
            ) {
                Text(buttonText)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    showSystemUi = true
)
@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun PermissionsDialogPreview() {
    MockLocationsTheme {
        Scaffold { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                PermissionsDialog(
                    permission = Permission.FineLocation
                )
            }
        }
    }
}
