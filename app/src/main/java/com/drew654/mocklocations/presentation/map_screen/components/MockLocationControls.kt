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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockLocationControls(
    mockControlState: MockControlState,
    controlsAreExpanded: Boolean,
    isShowingSearch: Boolean,
    modifier: Modifier = Modifier,
    onStart: () -> Unit = { },
    onStop: () -> Unit = { },
    onTogglePause: () -> Unit = { },
    onAddCrosshairsPoint: () -> Unit = { },
    onPopRouteSegment: () -> Unit = { },
    onClearLocationTarget: () -> Unit = { },
    onSaveLocationTarget: () -> Unit = { },
    setShowSearch: (Boolean) -> Unit = { },
    setControlsAreExpanded: (Boolean) -> Unit = { }
) {
    val focusManager = LocalFocusManager.current
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
        Spacer(Modifier.height((64 + 8).dp))
        SecondaryMockLocationControls(
            mockControlState = mockControlState,
            onClearLocationTarget = onClearLocationTarget,
            onSaveLocationTarget = onSaveLocationTarget,
            onPopRouteSegment = onPopRouteSegment,
            setShowSearch = setShowSearch,
            isShowingSearch = isShowingSearch,
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
                        mockControlState = mockControlState,
                        onStart = onStart,
                        onStop = onStop,
                        onTogglePause = onTogglePause,
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
                            focusManager.clearFocus()
                            setControlsAreExpanded(!controlsAreExpanded)
                        },
                        controlsAreExpanded = controlsAreExpanded
                    )

                    PrimaryMockLocationControls(
                        mockControlState = mockControlState,
                        onStart = onStart,
                        onStop = onStop,
                        onTogglePause = onTogglePause,
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
                mockControlState = MockControlState(),
                isShowingSearch = false,
                controlsAreExpanded = false
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
                mockControlState = MockControlState(
                    isMocking = true,
                    activeLocationTarget = LocationTarget.SinglePoint(
                        LatLng(0.0, 0.0)
                    )
                ),
                isShowingSearch = false,
                controlsAreExpanded = false
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
                mockControlState = MockControlState(),
                isShowingSearch = false,
                controlsAreExpanded = false
            )
        }
    }
}
