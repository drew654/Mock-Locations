package com.drew654.mocklocations.presentation.settings_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.drew654.mocklocations.presentation.NoRippleInteractionSource
import com.drew654.mocklocations.presentation.round
import com.drew654.mocklocations.presentation.toTrimmedString
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme
import java.lang.Float.parseFloat

@Composable
fun LocationUpdateDelayDialog(
    isVisible: Boolean,
    locationUpdateDelay: Float,
    onLocationUpdateDelayChanged: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss
        ) {
            val focusRequester = remember { FocusRequester() }
            val focusManager = LocalFocusManager.current
            var textFieldValue by remember {
                mutableStateOf(
                    TextFieldValue(
                        text = locationUpdateDelay.toTrimmedString(),
                        selection = TextRange(locationUpdateDelay.toTrimmedString().length)
                    )
                )
            }
            fun isValid(): Boolean = (textFieldValue.text.toFloatOrNull() ?: 0f) in 0.01f..999f
            fun handleSubmit() {
                if (isValid()) {
                    onLocationUpdateDelayChanged(parseFloat(textFieldValue.text).round())
                    onDismiss()
                }
            }

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            Card {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable(
                            interactionSource = NoRippleInteractionSource(),
                            indication = null
                        ) {
                            focusManager.clearFocus()
                        }
                ) {
                    Text(
                        text = "Location Update Delay",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = MaterialTheme.typography.titleLarge.fontWeight,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Delay between location updates in seconds",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                handleSubmit()
                            }
                        ),
                        singleLine = true
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
                                handleSubmit()
                            },
                            modifier = Modifier.padding(8.dp),
                            enabled = isValid()
                        ) {
                            Text(text = "Save Route")
                        }
                    }
                }
            }
        }
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
private fun LocationUpdateDelayDialogPreview() {
    MockLocationsTheme {
        Surface {
            LocationUpdateDelayDialog(
                isVisible = true,
                locationUpdateDelay = 1f,
                onLocationUpdateDelayChanged = { },
                onDismiss = { }
            )
        }
    }
}
