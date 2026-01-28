package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        SecondaryMockLocationControls(
            onClearClicked = onClearClicked,
            onSaveClicked = onSaveClicked,
            onPopClicked = onPopClicked,
            locationTarget = locationTarget,
            isMocking = isMocking,
            scrollState = scrollState,
            modifier = Modifier.weight(1f, fill = false)
        )
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

                    PrimaryMockLocationControls(
                        onPlayClicked = onPlayClicked,
                        onStopClicked = onStopClicked,
                        onPauseClicked = onPauseClicked,
                        isMocking = isMocking,
                        isPaused = isPaused,
                        locationTarget = locationTarget,
                        useCrosshairs = useCrosshairs,
                        onAddCrosshairsPoint = onAddCrosshairsPoint
                    )
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

                    PrimaryMockLocationControls(
                        onPlayClicked = onPlayClicked,
                        onStopClicked = onStopClicked,
                        onPauseClicked = onPauseClicked,
                        isMocking = isMocking,
                        isPaused = isPaused,
                        locationTarget = locationTarget,
                        useCrosshairs = useCrosshairs,
                        onAddCrosshairsPoint = onAddCrosshairsPoint,
                        modifier = Modifier.weight(1f)
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
