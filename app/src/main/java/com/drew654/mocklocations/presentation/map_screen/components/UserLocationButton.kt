package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import com.drew654.mocklocations.R
import com.drew654.mocklocations.presentation.ui.theme.DayNightPreviews
import com.drew654.mocklocations.presentation.ui.theme.ThemePreview

@Composable
fun UserLocationButton(
    onClick: () -> Unit = { }
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

@DayNightPreviews
@Composable
fun UserLocationButtonPreview() {
    ThemePreview {
        UserLocationButton()
    }
}
