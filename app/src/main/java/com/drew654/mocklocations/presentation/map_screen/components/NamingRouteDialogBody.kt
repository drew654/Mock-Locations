package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.presentation.NoRippleInteractionSource

@Composable
fun NamingRouteDialogBody(
    routeName: String,
    onRouteNameChange: (String) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val focusManager = LocalFocusManager.current

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
            label = { Text("Route Name") },
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .fillMaxWidth(),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onConfirm()
                }
            )
        )
        Row {
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = {
                    onBack()
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Back")
            }
            TextButton(
                onClick = {
                    onConfirm()
                },
                modifier = Modifier.padding(8.dp),
                enabled = routeName.isNotBlank()
            ) {
                Text(text = "Save Route")
            }
        }
    }
}
