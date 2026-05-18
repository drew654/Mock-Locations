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
fun StartMockingButton(
    modifier: Modifier = Modifier,
    onStart: () -> Unit = { },
    enabled: Boolean = true
) {
    DisableableFloatingActionButton(
        onClick = { onStart() },
        enabled = enabled,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_play_arrow_24),
            contentDescription = "Start"
        )
    }
}

@DayNightPreviews
@Composable
fun StartMockingButtonPreview() {
    ThemePreview {
        Box(modifier = Modifier.padding(8.dp)) {
            StartMockingButton()
        }
    }
}
