package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.presentation.NoRippleInteractionSource
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

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
fun NamingRouteDialogBodyPreview() {
    MockLocationsTheme {
        Surface {
            Card {
                NamingRouteDialogBody(
                    routeName = "Route 1",
                    onRouteNameChange = {},
                    onBack = {},
                    onConfirm = {}
                )
            }
        }
    }
}
