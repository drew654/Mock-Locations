package com.drew654.mocklocations.presentation.manage_routes.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.drew654.mocklocations.presentation.NoRippleInteractionSource
import com.drew654.mocklocations.presentation.ui.theme.DayNightDevicePreviews
import com.drew654.mocklocations.presentation.ui.theme.DeviceThemePreview

@Composable
fun RenameRouteDialog(
    routeName: String,
    isSaveRouteEnabled: Boolean = true,
    onRouteNameChange: (String) -> Unit = { },
    onDismiss: () -> Unit = { },
    onConfirm: () -> Unit = { },
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Dialog(
        onDismissRequest = {
            onDismiss()
        }
    ) {
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = NoRippleInteractionSource(),
                        indication = null
                    ) {
                        focusManager.clearFocus()
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Save Route",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                OutlinedTextField(
                    value = routeName,
                    onValueChange = { onRouteNameChange(it) },
                    label = { Text("Route name") },
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (routeName.isNotBlank()) {
                                onConfirm()
                            }
                        }
                    )
                )

                Row {
                    Spacer(Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            onDismiss()
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = "Cancel")
                    }
                    TextButton(
                        onClick = {
                            onConfirm()
                        },
                        modifier = Modifier.padding(8.dp),
                        enabled = isSaveRouteEnabled
                    ) {
                        Text(text = "Save route")
                    }
                }
            }
        }
    }
}

@DayNightDevicePreviews
@Composable
private fun RenameRouteDialogPreview() {
    DeviceThemePreview {
        RenameRouteDialog(
            routeName = "Route 1"
        )
    }
}
