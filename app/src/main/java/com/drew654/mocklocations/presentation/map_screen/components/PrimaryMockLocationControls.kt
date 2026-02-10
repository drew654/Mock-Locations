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
    onStart: () -> Unit,
    onStop: () -> Unit,
    onTogglePause: () -> Unit,
    isMocking: Boolean,
    isPaused: Boolean,
    locationTarget: LocationTarget,
    isUsingCrosshairs: Boolean,
    onAddCrosshairsPoint: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldShowStartButton = !isMocking
    val shouldShowStopButton = isMocking
    val shouldShowResumeButton = isMocking && isPaused && (locationTarget is LocationTarget.Route || locationTarget is LocationTarget.SavedRoute)
    val shouldShowPauseButton = isMocking && !isPaused && (locationTarget is LocationTarget.Route || locationTarget is LocationTarget.SavedRoute)
    val shouldShowAddPointButton = !isMocking && isUsingCrosshairs

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
            if (shouldShowAddPointButton) {
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

            if (shouldShowResumeButton) {
                DisableableSmallFloatingActionButton(
                    onClick = { onTogglePause() },
                    enabled = true,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                        contentDescription = "Resume",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.width(12.dp))
            }

            if (shouldShowPauseButton) {
                DisableableSmallFloatingActionButton(
                    onClick = { onTogglePause() },
                    enabled = true,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_pause_24),
                        contentDescription = "Pause",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.width(12.dp))
            }

            if (shouldShowStartButton) {
                DisableableFloatingActionButton(
                    onClick = {
                        onStart()
                    },
                    enabled = isUsingCrosshairs || locationTarget !is LocationTarget.Empty,
                    modifier = Modifier.padding(bottom = 12.dp, end = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                        contentDescription = "Start"
                    )
                }
            }

            if (shouldShowStopButton) {
                DisableableFloatingActionButton(
                    onClick = {
                        onStop()
                    },
                    enabled = true,
                    modifier = Modifier.padding(bottom = 12.dp, end = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_stop_24),
                        contentDescription = "Stop"
                    )
                }
            }
        }
    }
}
