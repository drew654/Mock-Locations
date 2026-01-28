package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.LocationTarget

@Composable
fun PrimaryMockLocationControls(
    onPlayClicked: () -> Unit,
    onStopClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    isMocking: Boolean,
    isPaused: Boolean,
    locationTarget: LocationTarget,
    useCrosshairs: Boolean,
    onAddCrosshairsPoint: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(start = 12.dp)
            .windowInsetsPadding(
                WindowInsets.displayCutout.only(
                    WindowInsetsSides.Horizontal
                )
            ),
        contentAlignment = Alignment.BottomEnd
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isMocking && useCrosshairs) {
                DisableableFloatingActionButton(
                    onClick = { onAddCrosshairsPoint() },
                    enabled = true,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_add_location_alt_24),
                        contentDescription = "Add Point"
                    )
                }
                Spacer(Modifier.width(12.dp))
            }

            if (isMocking && (locationTarget is LocationTarget.Route || locationTarget is LocationTarget.SavedRoute)) {
                DisableableSmallFloatingActionButton(
                    onClick = { onPauseClicked() },
                    enabled = true,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = if (isPaused) R.drawable.baseline_play_arrow_24 else R.drawable.baseline_pause_24),
                        contentDescription = if (isPaused) "Resume" else "Pause",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.width(12.dp))
            }

            DisableableFloatingActionButton(
                onClick = {
                    if (isMocking) onStopClicked() else onPlayClicked()
                },
                enabled = locationTarget !is LocationTarget.Empty,
                modifier = Modifier.padding(bottom = 12.dp, end = 12.dp)
            ) {
                Icon(
                    painter = painterResource(id = if (isMocking) R.drawable.baseline_stop_24 else R.drawable.baseline_play_arrow_24),
                    contentDescription = if (isMocking) "Stop" else "Play"
                )
            }
        }
    }
}
