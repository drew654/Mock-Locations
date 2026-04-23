package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.R
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

@Composable
fun SearchAddressSection(
    onSearchAddress: (String) -> Unit,
    shouldFocusSearchBar: Boolean
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var hasFocusBeenRequested by remember { mutableStateOf(!shouldFocusSearchBar) }
    var address by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    LaunchedEffect(Unit) {
        if (!hasFocusBeenRequested) {
            focusRequester.requestFocus()
            hasFocusBeenRequested = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TextField(
            value = address,
            onValueChange = {
                address = it
            },
            label = { Text("Search address") },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_search_24),
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (address.text.isNotBlank()) {
                    IconButton(
                        onClick = {
                            address = TextFieldValue("")
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_clear_24),
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search,
                capitalization = KeyboardCapitalization.None
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearchAddress(address.text)
                    focusManager.clearFocus()
                }
            ),
            singleLine = true,
            maxLines = 1,
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth()
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
private fun SearchAddressSectionPreview() {
    MockLocationsTheme {
        Surface {
            SearchAddressSection(
                onSearchAddress = { },
                shouldFocusSearchBar = true
            )
        }
    }
}
