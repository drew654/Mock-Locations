package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.drew654.mocklocations.R
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

@Composable
fun UserLocationButton(
    onClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    SmallFloatingActionButton(
        onClick = {
            focusManager.clearFocus()
            onClick()
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_my_location_24),
            contentDescription = "My Location",
            tint = MaterialTheme.colorScheme.onSurface
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
fun UserLocationButtonPreview() {
    MockLocationsTheme {
        Surface {
            UserLocationButton(
                onClick = { }
            )
        }
    }
}
