package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockLocationControls(
    onClearClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    onStopClicked: () -> Unit,
    onPopClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    locationTarget: LocationTarget,
    isMocking: Boolean,
    isPaused: Boolean,
    onSaveClicked: () -> Unit,
    onAddCrosshairsPoint: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(scrollState.maxValue) {
        if (scrollState.maxValue > 0) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(
                    scrollState
                )
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
        }
        Spacer(Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isMocking) {
                DisableableFloatingActionButton(
                    onClick = {
                        onAddCrosshairsPoint()
                    },
                    enabled = true
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
                    onClick = {
                        onPauseClicked()
                    },
                    enabled = true
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
                    if (isMocking) {
                        onStopClicked()
                    } else {
                        onPlayClicked()
                    }
                },
                enabled = locationTarget !is LocationTarget.Empty
            ) {
                if (isMocking) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_stop_24),
                        contentDescription = "Stop"
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                        contentDescription = "Play"
                    )
                }
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
fun MockLocationControlsPreview1() {
    MockLocationsTheme {
        Surface {
            MockLocationControls(
                onClearClicked = { },
                onPlayClicked = { },
                onStopClicked = { },
                onPopClicked = { },
                onPauseClicked = { },
                locationTarget = LocationTarget.Empty,
                isMocking = false,
                isPaused = false,
                onSaveClicked = { },
                onAddCrosshairsPoint = { }
            )
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
fun MockLocationControlsPreview2() {
    MockLocationsTheme {
        Surface {
            MockLocationControls(
                onClearClicked = { },
                onPlayClicked = { },
                onStopClicked = { },
                onPopClicked = { },
                onPauseClicked = { },
                locationTarget = LocationTarget.Route(listOf(LatLng(0.0, 0.0), LatLng(0.0, 0.0))),
                isMocking = true,
                isPaused = false,
                onSaveClicked = { },
                onAddCrosshairsPoint = { }
            )
        }
    }
}
