package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
    useCrosshairs: Boolean,
    onAddCrosshairsPoint: () -> Unit,
    controlsAreExpanded: Boolean,
    setControlsAreExpanded: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val minWidth = 384.dp
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
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val isNarrow = maxWidth < minWidth
            if (isNarrow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.End
                ) {
                    ExpandControlsButton(
                        onClick = {
                            setControlsAreExpanded(!controlsAreExpanded)
                        },
                        controlsAreExpanded = controlsAreExpanded
                    )

                    Box(
                        modifier = Modifier
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
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    ExpandControlsButton(
                        onClick = {
                            setControlsAreExpanded(!controlsAreExpanded)
                        },
                        controlsAreExpanded = controlsAreExpanded
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
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
private fun MockLocationControlsPreview1() {
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
                useCrosshairs = true,
                onAddCrosshairsPoint = { },
                controlsAreExpanded = false,
                setControlsAreExpanded = { }
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
private fun MockLocationControlsPreview2() {
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
                useCrosshairs = false,
                onAddCrosshairsPoint = { },
                controlsAreExpanded = false,
                setControlsAreExpanded = { }
            )
        }
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    widthDp = 360
)
@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    widthDp = 360
)
@Composable
private fun MockLocationControlsPreviewNarrow() {
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
                useCrosshairs = true,
                onAddCrosshairsPoint = { },
                controlsAreExpanded = false,
                setControlsAreExpanded = { }
            )
        }
    }
}
