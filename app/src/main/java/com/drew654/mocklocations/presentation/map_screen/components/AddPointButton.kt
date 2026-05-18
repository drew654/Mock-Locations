package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.R
import com.drew654.mocklocations.presentation.ui.theme.DayNightPreviews
import com.drew654.mocklocations.presentation.ui.theme.ThemePreview

@Composable
fun AddPointButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { },
    enabled: Boolean = true
) {
    DisableableFloatingActionButton(
        onClick = { onClick() },
        enabled = enabled,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_add_location_alt_24),
            contentDescription = "Add Point"
        )
    }
}

@DayNightPreviews
@Composable
fun AddPointButtonPreview() {
    ThemePreview {
        Box(modifier = Modifier.padding(8.dp)) {
            AddPointButton()
        }
    }
}
