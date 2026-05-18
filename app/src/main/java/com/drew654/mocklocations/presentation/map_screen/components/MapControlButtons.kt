package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.CompassState
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.isAddPointVisible
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

@Composable
fun MapControlButtons(
    mockControlState: MockControlState,
    controlsAreExpanded: Boolean,
    isPaused: Boolean,
    isShowingSearch: Boolean,
    compassState: CompassState,
    crosshairsColor: Color,
    onStart: () -> Unit = { },
    onStop: () -> Unit = { },
    onTogglePause: () -> Unit = { },
    onAddCrosshairsPoint: () -> Unit = { },
    onPopRouteSegment: () -> Unit = { },
    onClearLocationTarget: () -> Unit = { },
    onSaveLocationTarget: () -> Unit = { },
    setShowSearch: (Boolean) -> Unit = { },
    setControlsAreExpanded: (Boolean) -> Unit = { },
    onUserLocationFocus: () -> Unit = { },
    onClickCompass: () -> Unit = { },
    onSettingsClick: () -> Unit = { },
    onZoomIn: () -> Unit = { },
    onZoomOut: () -> Unit = { }
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.displayCutout.only(
                        WindowInsetsSides.Horizontal
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                ) {
                    UserLocationButton(
                        onClick = {
                            onUserLocationFocus()
                        }
                    )
                    if (compassState.isVisible) {
                        Spacer(Modifier.height(4.dp))
                        CompassButton(
                            bearing = compassState.bearing,
                            onClick = { onClickCompass() }
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                MapZoomButtons(
                    modifier = Modifier
                        .padding(12.dp)
                        .padding(bottom = 32.dp),
                    onZoomIn = {
                        onZoomIn()
                    },
                    onZoomOut = {
                        onZoomOut()
                    }
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        onSettingsClick()
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 12.dp, end = 12.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_settings_24),
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.weight(1f))
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            MockLocationControls(
                mockControlState = mockControlState,
                onClearLocationTarget = {
                    onClearLocationTarget()
                },
                onStart = {
                    if (isPaused) {
                        onTogglePause()
                    } else {
                        onStart()
                    }
                },
                onStop = {
                    onStop()
                },
                onPopRouteSegment = {
                    onPopRouteSegment()
                },
                onTogglePause = {
                    onTogglePause()
                },
                onSaveLocationTarget = {
                    onSaveLocationTarget()
                },
                onAddCrosshairsPoint = {
                    onAddCrosshairsPoint()
                },
                setShowSearch = setShowSearch,
                isShowingSearch = isShowingSearch,
                controlsAreExpanded = controlsAreExpanded,
                setControlsAreExpanded = {
                    setControlsAreExpanded(it)
                },
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (mockControlState.isAddPointVisible()) {
                Crosshairs(color = crosshairsColor, modifier = Modifier.align(Alignment.Center))
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
fun MapControlButtonsPreview() {
    MockLocationsTheme {
        Surface {
            MapControlButtons(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isPaused = false,
                isShowingSearch = false,
                crosshairsColor = MaterialTheme.colorScheme.onSurface,
                compassState = CompassState(isVisible = true, bearing = 0f)
            )
        }
    }
}
