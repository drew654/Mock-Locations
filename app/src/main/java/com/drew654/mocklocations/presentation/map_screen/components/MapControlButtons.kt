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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.MockControlAction
import com.drew654.mocklocations.presentation.Screen
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme
import com.google.maps.android.compose.CameraPositionState

@Composable
fun MapControlButtons(
    navController: NavController,
    visibleMockControlActions: Set<MockControlAction>,
    enabledMockControlActions: Set<MockControlAction>,
    cameraPositionState: CameraPositionState,
    controlsAreExpanded: Boolean,
    setControlsAreExpanded: (Boolean) -> Unit,
    onClearLocationTarget: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onPopPoint: () -> Unit,
    onTogglePause: () -> Unit,
    onSaveLocationTarget: () -> Unit,
    isPaused: Boolean,
    onAddCrosshairsPoint: () -> Unit,
    onUserLocationFocus: () -> Unit,
    isCameraCurrentlyFollowingMockedLocation: Boolean
) {
    val scope = rememberCoroutineScope()

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
                    if (cameraPositionState.position.bearing != 0f || cameraPositionState.position.tilt != 0f) {
                        Spacer(Modifier.height(4.dp))
                        CompassButton(
                            scope = scope,
                            cameraPositionState = cameraPositionState
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                MapZoomButtons(
                    cameraPositionState = cameraPositionState,
                    scope = scope,
                    isCameraCurrentlyFollowingMockedLocation = isCameraCurrentlyFollowingMockedLocation,
                    modifier = Modifier
                        .padding(12.dp)
                        .padding(bottom = 32.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.Settings.route)
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
                visibleMockControlActions = visibleMockControlActions,
                enabledMockControlActions = enabledMockControlActions,
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
                onPopPoint = {
                    onPopPoint()
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
            if (MockControlAction.ADD_POINT in visibleMockControlActions) {
                Crosshairs(modifier = Modifier.align(Alignment.Center))
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
                navController = NavController(LocalContext.current),
                visibleMockControlActions = setOf(
                    MockControlAction.START,
                    MockControlAction.ADD_POINT
                ),
                enabledMockControlActions = setOf(
                    MockControlAction.START,
                    MockControlAction.ADD_POINT
                ),
                cameraPositionState = CameraPositionState(),
                controlsAreExpanded = false,
                setControlsAreExpanded = { },
                onClearLocationTarget = { },
                onStart = { },
                onStop = { },
                onPopPoint = { },
                onTogglePause = { },
                onSaveLocationTarget = { },
                isPaused = false,
                onAddCrosshairsPoint = { },
                onUserLocationFocus = { },
                isCameraCurrentlyFollowingMockedLocation = false
            )
        }
    }
}
