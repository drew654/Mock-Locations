package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.R
import com.drew654.mocklocations.presentation.ui.theme.DayNightPreviews
import com.drew654.mocklocations.presentation.ui.theme.ThemePreview

@Composable
fun SavedRoutesButton(
    onClick: () -> Unit = { }
) {
    DisableableSmallFloatingActionButton(
        onClick = { onClick() },
        enabled = true
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_save_24),
            contentDescription = "Saved Routes",
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@DayNightPreviews
@Composable
fun SavedRoutesButtonPreview() {
    ThemePreview {
        Box(modifier = Modifier.padding(4.dp)) {
            SavedRoutesButton()
        }
    }
}
