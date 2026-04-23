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
import com.drew654.mocklocations.domain.model.MockControlAction
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockLocationControls(
    visibleMockControlActions: Set<MockControlAction>,
    enabledMockControlActions: Set<MockControlAction>,
    onClearLocationTarget: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onPopPoint: () -> Unit,
    onTogglePause: () -> Unit,
    onSaveLocationTarget: () -> Unit,
    onAddCrosshairsPoint: () -> Unit,
    setShowSearch: (Boolean) -> Unit,
    isShowingSearch: Boolean,
    controlsAreExpanded: Boolean,
    setControlsAreExpanded: (Boolean) -> Unit,
    modifier: Modifier = Modifier
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
            enabledMockControlActions = enabledMockControlActions,
            onClearLocationTarget = onClearLocationTarget,
            onSaveLocationTarget = onSaveLocationTarget,
            onPopPoint = onPopPoint,
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
                        visibleMockControlActions = visibleMockControlActions,
                        enabledMockControlActions = enabledMockControlActions,
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
                        visibleMockControlActions = visibleMockControlActions,
                        enabledMockControlActions = enabledMockControlActions,
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
                visibleMockControlActions = setOf(
                    MockControlAction.START,
                    MockControlAction.ADD_POINT
                ),
                enabledMockControlActions = setOf(
                    MockControlAction.START,
                    MockControlAction.ADD_POINT
                ),
                onClearLocationTarget = { },
                onStart = { },
                onStop = { },
                onPopPoint = { },
                onTogglePause = { },
                onSaveLocationTarget = { },
                onAddCrosshairsPoint = { },
                setShowSearch = { },
                isShowingSearch = false,
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
                visibleMockControlActions = setOf(
                    MockControlAction.STOP,
                    MockControlAction.PAUSE
                ),
                enabledMockControlActions = setOf(
                    MockControlAction.STOP,
                    MockControlAction.PAUSE
                ),
                onClearLocationTarget = { },
                onStart = { },
                onStop = { },
                onPopPoint = { },
                onTogglePause = { },
                onSaveLocationTarget = { },
                onAddCrosshairsPoint = { },
                setShowSearch = { },
                isShowingSearch = false,
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
                visibleMockControlActions = setOf(
                    MockControlAction.START,
                    MockControlAction.ADD_POINT
                ),
                enabledMockControlActions = setOf(
                    MockControlAction.START,
                    MockControlAction.ADD_POINT
                ),
                onClearLocationTarget = { },
                onStart = { },
                onStop = { },
                onPopPoint = { },
                onTogglePause = { },
                onSaveLocationTarget = { },
                onAddCrosshairsPoint = { },
                setShowSearch = { },
                isShowingSearch = false,
                controlsAreExpanded = false,
                setControlsAreExpanded = { }
            )
        }
    }
}
