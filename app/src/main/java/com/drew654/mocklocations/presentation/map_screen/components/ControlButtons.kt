package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme
import com.google.android.gms.maps.model.LatLng

@Composable
fun ControlButtons(
    onClearClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    onStopClicked: () -> Unit,
    onPopClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    speedMetersPerSec: Double,
    onSpeedChanged: (Double) -> Unit,
    points: List<LatLng>,
    isMocking: Boolean,
    isPaused: Boolean
) {
    var speedSliderIsExpanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.padding(12.dp)
    ) {
        Row {
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
                Slider(
                    value = speedMetersPerSec.toFloat(),
                    onValueChange = {
                        onSpeedChanged(it.toDouble())
                    },
                    valueRange = 0f..100f,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        DisableableSmallFloatingActionButton(
            onClick = {
                onPopClicked()
            },
            enabled = points.isNotEmpty() && !isMocking
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_backspace_24),
                contentDescription = "Pop",
                tint = if (points.isEmpty())
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
            enabled = points.isNotEmpty() && !isMocking
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_clear_24),
                contentDescription = "Clear",
                tint = if (points.isEmpty())
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
                    if (isMocking && !isPaused) {
                        onStopClicked()
                    } else {
                        onPlayClicked()
                    }
                },
                enabled = points.isNotEmpty()
            ) {
                if (isMocking && !isPaused) {
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
            if (isMocking && !isPaused && points.size > 1) {
                Spacer(Modifier.width(12.dp))
                DisableableSmallFloatingActionButton(
                    onClick = {
                        onPauseClicked()
                    },
                    enabled = points.isNotEmpty()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_pause_24),
                        contentDescription = "Pause",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                points = emptyList(),
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
                points = listOf(LatLng(0.0, 0.0), LatLng(0.0, 0.0)),
                isMocking = true,
                isPaused = false
            )
        }
    }
}
