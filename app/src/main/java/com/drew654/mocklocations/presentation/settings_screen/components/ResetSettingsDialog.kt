package com.drew654.mocklocations.presentation.settings_screen.components

import android.content.res.Configuration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

@Composable
fun ResetSettingsDialog(
    isVisible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
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

@Preview(
    name = "Light Mode",
    showBackground = true
)
@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun ResetSettingsDialogPreview() {
    MockLocationsTheme {
        Surface {
            ResetSettingsDialog(
                isVisible = true,
                onConfirm = { },
                onDismiss = { }
            )
        }
    }
}
