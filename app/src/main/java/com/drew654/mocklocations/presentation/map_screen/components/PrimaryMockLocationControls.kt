package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.domain.model.MockControlAction
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

@Composable
fun PrimaryMockLocationControls(
    visibleMockControlActions: Set<MockControlAction>,
    enabledMockControlActions: Set<MockControlAction>,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onTogglePause: () -> Unit,
    onAddCrosshairsPoint: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
            if (MockControlAction.ADD_POINT in visibleMockControlActions) {
                AddPointButton(
                    onAddCrosshairsPoint = onAddCrosshairsPoint,
                    enabledMockControlActions = enabledMockControlActions,
                    modifier = Modifier.padding(end = 12.dp, bottom = 12.dp)
                )
            }

            if (MockControlAction.RESUME in visibleMockControlActions) {
                ResumeMockingButton(
                    onTogglePause = onTogglePause,
                    enabledMockControlActions = enabledMockControlActions,
                    modifier = Modifier.padding(end = 12.dp, bottom = 12.dp)
                )
            }

            if (MockControlAction.PAUSE in visibleMockControlActions) {
                PauseMockingButton(
                    onTogglePause = onTogglePause,
                    enabledMockControlActions = enabledMockControlActions,
                    modifier = Modifier.padding(end = 12.dp, bottom = 12.dp)
                )
            }

            if (MockControlAction.START in visibleMockControlActions) {
                StartMockingButton(
                    onStart = onStart,
                    enabledMockControlActions = enabledMockControlActions,
                    modifier = Modifier.padding(bottom = 12.dp, end = 12.dp)
                )
            }

            if (MockControlAction.STOP in visibleMockControlActions) {
                StopMockingButton(
                    onStop = onStop,
                    enabledMockControlActions = enabledMockControlActions,
                    modifier = Modifier.padding(bottom = 12.dp, end = 12.dp)
                )
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
private fun PrimaryMockLocationControlsPreview() {
    MockLocationsTheme {
        Surface {
            PrimaryMockLocationControls(
                visibleMockControlActions = setOf(
                    MockControlAction.START,
                    MockControlAction.ADD_POINT
                ),
                enabledMockControlActions = setOf(
                    MockControlAction.START,
                    MockControlAction.ADD_POINT
                ),
                onStart = { },
                onStop = { },
                onTogglePause = { },
                onAddCrosshairsPoint = { }
            )
        }
    }
}
