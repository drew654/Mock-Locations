package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
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
    speedMetersPerSec: Double,
    onSpeedChanged: (Double) -> Unit,
    onSpeedChangeFinished: (Double) -> Unit,
    points: List<LatLng>,
    isMocking: Boolean,
    isPaused: Boolean
) {
    var speedSliderIsExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.displayCutout.only(
                    androidx.compose.foundation.layout.WindowInsetsSides.Horizontal
                )
            ),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
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
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .widthIn(max = 300.dp)
                                .fillMaxWidth(),
                            onValueChangeFinished = {
                                onSpeedChangeFinished(speedMetersPerSec)
                            },
                            valueRange = 0f..100f
                        )
                    }
                }
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
                        if (isMocking) {
                            onStopClicked()
                        } else {
                            onPlayClicked()
                        }
                    },
                    enabled = points.isNotEmpty()
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
                if (isMocking && points.size > 1) {
                    Spacer(Modifier.width(12.dp))
                    DisableableSmallFloatingActionButton(
                        onClick = {
                            onPauseClicked()
                        },
                        enabled = points.isNotEmpty()
                    ) {
                        Icon(
                            painter = painterResource(id = if (isPaused) R.drawable.baseline_play_arrow_24 else (R.drawable.baseline_pause_24)),
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
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
                onClearClicked = {},
                onPlayClicked = {},
                onStopClicked = {},
                onPopClicked = {},
                onPauseClicked = {},
                speedMetersPerSec = 30.0,
                onSpeedChanged = {},
                onSpeedChangeFinished = {},
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
fun MockLocationControlsPreview2() {
    MockLocationsTheme {
        Surface {
            MockLocationControls(
                onClearClicked = {},
                onPlayClicked = {},
                onStopClicked = {},
                onPopClicked = {},
                onPauseClicked = {},
                speedMetersPerSec = 30.0,
                onSpeedChanged = {},
                onSpeedChangeFinished = {},
                points = listOf(LatLng(0.0, 0.0), LatLng(0.0, 0.0)),
                isMocking = true,
                isPaused = false
            )
        }
    }
}
