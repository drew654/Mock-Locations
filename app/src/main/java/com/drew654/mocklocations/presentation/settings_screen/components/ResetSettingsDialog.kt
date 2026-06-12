package com.drew654.mocklocations.presentation.settings_screen.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.drew654.mocklocations.presentation.ui.theme.DayNightDevicePreviews
import com.drew654.mocklocations.presentation.ui.theme.DayNightTabletPreviews
import com.drew654.mocklocations.presentation.ui.theme.DeviceThemePreview

@Composable
fun ResetSettingsDialog(
    isVisible: Boolean,
    onConfirm: () -> Unit = { },
    onDismiss: () -> Unit = { }
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = {
                Text("Reset settings to default?")
            },
            confirmButton = {
                TextButton(onClick = { onConfirm() }) {
                    Text("Reset Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@DayNightDevicePreviews
@DayNightTabletPreviews
@Composable
private fun ResetSettingsDialogPhonePreview() {
    DeviceThemePreview {
        ResetSettingsDialog(
            isVisible = true
        )
    }
}
