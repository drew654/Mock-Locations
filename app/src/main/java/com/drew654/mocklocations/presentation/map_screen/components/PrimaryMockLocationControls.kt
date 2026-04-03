package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.R
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

            if (MockControlAction.RESUME in visibleMockControlActions) {
                DisableableSmallFloatingActionButton(
                    onClick = { onTogglePause() },
                    enabled = true,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                        contentDescription = "Resume",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.width(12.dp))
            }

            if (MockControlAction.PAUSE in visibleMockControlActions) {
                DisableableSmallFloatingActionButton(
                    onClick = { onTogglePause() },
                    enabled = true,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_pause_24),
                        contentDescription = "Pause",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.width(12.dp))
            }

            if (MockControlAction.START in visibleMockControlActions) {
                DisableableFloatingActionButton(
                    onClick = {
                        onStart()
                    },
                    enabled = MockControlAction.START in enabledMockControlActions,
                    modifier = Modifier.padding(bottom = 12.dp, end = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                        contentDescription = "Start"
                    )
                }
            }

            if (MockControlAction.STOP in visibleMockControlActions) {
                DisableableFloatingActionButton(
                    onClick = {
                        onStop()
                    },
                    enabled = true,
                    modifier = Modifier.padding(bottom = 12.dp, end = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_stop_24),
                        contentDescription = "Stop"
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
