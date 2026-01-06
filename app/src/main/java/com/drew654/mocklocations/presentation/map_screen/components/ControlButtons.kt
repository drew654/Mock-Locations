package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun ControlButtons(
    onClearClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    onStopClicked: () -> Unit,
    onPopClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onSaveClicked: () -> Unit,
    speedMetersPerSec: Double,
    onSpeedChanged: (Double) -> Unit,
    locationTarget: LocationTarget,
    isMocking: Boolean,
    isPaused: Boolean
) {
    var speedSliderIsExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(12.dp)
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
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            DisableableSmallFloatingActionButton(
                onClick = {
                    speedSliderIsExpanded = !speedSliderIsExpanded
                },
                enabled = true
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_speed_24),
                    contentDescription = "Toggle Speed Slider",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            if (speedSliderIsExpanded) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${speedMetersPerSec.toInt()} m/s",
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = speedMetersPerSec.toFloat(),
                        onValueChange = {
                            onSpeedChanged(it.toDouble())
                        },
                        valueRange = 0f..100f
                    )
                }
            }
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
        Spacer(Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
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
            if (isMocking && locationTarget is LocationTarget.Route) {
                Spacer(Modifier.width(12.dp))
                DisableableSmallFloatingActionButton(
                    onClick = {
                        onPauseClicked()
                    },
                    enabled = true
                ) {
                    Icon(
                        painter = painterResource(id = if (isPaused) R.drawable.baseline_play_arrow_24 else (R.drawable.baseline_pause_24)),
                        contentDescription = if (isPaused) "Resume" else "Pause",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        Spacer(Modifier.height(32.dp))
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
fun ControlButtonsPreview1() {
    MockLocationsTheme {
        Surface {
            ControlButtons(
                onClearClicked = {},
                onPlayClicked = {},
                onStopClicked = {},
                onPopClicked = {},
                onPauseClicked = {},
                speedMetersPerSec = 30.0,
                onSpeedChanged = {},
                onSaveClicked = {},
                locationTarget = LocationTarget.Empty,
                isMocking = false,
                isPaused = false
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
fun ControlButtonsPreview2() {
    MockLocationsTheme {
        Surface {
            ControlButtons(
                onClearClicked = {},
                onPlayClicked = {},
                onStopClicked = {},
                onPopClicked = {},
                onPauseClicked = {},
                speedMetersPerSec = 30.0,
                onSpeedChanged = {},
                onSaveClicked = {},
                locationTarget = LocationTarget.create(listOf(LatLng(0.0, 0.0), LatLng(0.0, 0.0))),
                isMocking = true,
                isPaused = false
            )
        }
    }
}
