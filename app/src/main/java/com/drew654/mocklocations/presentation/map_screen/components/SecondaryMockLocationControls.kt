package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.LocationTarget

@Composable
fun SecondaryMockLocationControls(
    onClearClicked: () -> Unit,
    onSaveClicked: () -> Unit,
    onPopClicked: () -> Unit,
    locationTarget: LocationTarget,
    isMocking: Boolean,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(
                scrollState
            )
            .windowInsetsPadding(
                WindowInsets.displayCutout.only(
                    WindowInsetsSides.Horizontal
                )
            )
            .padding(bottom = 12.dp, end = 12.dp)
    ) {
        DisableableSmallFloatingActionButton(
            onClick = {
                onSaveClicked()
            },
            enabled = true
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_save_24),
                contentDescription = "Saved Routes",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.height(4.dp))
        DisableableSmallFloatingActionButton(
            onClick = {
                onClearClicked()
            },
            enabled = locationTarget !is LocationTarget.Empty && !isMocking
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_clear_24),
                contentDescription = "Clear",
                tint = if (locationTarget is LocationTarget.Empty)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.height(4.dp))
        DisableableSmallFloatingActionButton(
            onClick = {
                onPopClicked()
            },
            enabled = locationTarget !is LocationTarget.Empty && !isMocking
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_backspace_24),
                contentDescription = "Pop",
                tint = if (locationTarget is LocationTarget.Empty)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
