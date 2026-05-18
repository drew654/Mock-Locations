package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.isAddPointEnabled
import com.drew654.mocklocations.domain.model.isAddPointVisible
import com.drew654.mocklocations.domain.model.isPauseEnabled
import com.drew654.mocklocations.domain.model.isPauseVisible
import com.drew654.mocklocations.domain.model.isResumeEnabled
import com.drew654.mocklocations.domain.model.isResumeVisible
import com.drew654.mocklocations.domain.model.isStartEnabled
import com.drew654.mocklocations.domain.model.isStartVisible
import com.drew654.mocklocations.domain.model.isStopEnabled
import com.drew654.mocklocations.domain.model.isStopVisible
import com.drew654.mocklocations.presentation.ui.theme.DayNightPreviews
import com.drew654.mocklocations.presentation.ui.theme.ThemePreview

@Composable
fun PrimaryMockLocationControls(
    mockControlState: MockControlState,
    modifier: Modifier = Modifier,
    onStart: () -> Unit = { },
    onStop: () -> Unit = { },
    onTogglePause: () -> Unit = { },
    onAddCrosshairsPoint: () -> Unit = { }
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
            if (mockControlState.isAddPointVisible()) {
                AddPointButton(
                    onClick = onAddCrosshairsPoint,
                    enabled = mockControlState.isAddPointEnabled(),
                    modifier = Modifier.padding(end = 12.dp, bottom = 12.dp)
                )
            }

            if (mockControlState.isResumeVisible()) {
                ResumeMockingButton(
                    onTogglePause = onTogglePause,
                    enabled = mockControlState.isResumeEnabled(),
                    modifier = Modifier.padding(end = 12.dp, bottom = 12.dp)
                )
            }

            if (mockControlState.isPauseVisible()) {
                PauseMockingButton(
                    onClick = onTogglePause,
                    enabled = mockControlState.isPauseEnabled(),
                    modifier = Modifier.padding(end = 12.dp, bottom = 12.dp)
                )
            }

            if (mockControlState.isStartVisible()) {
                StartMockingButton(
                    onStart = onStart,
                    enabled = mockControlState.isStartEnabled(),
                    modifier = Modifier.padding(bottom = 12.dp, end = 12.dp)
                )
            }

            if (mockControlState.isStopVisible()) {
                StopMockingButton(
                    onStop = onStop,
                    enabled = mockControlState.isStopEnabled(),
                    modifier = Modifier.padding(bottom = 12.dp, end = 12.dp)
                )
            }
        }
    }
}

@DayNightPreviews
@Composable
private fun PrimaryMockLocationControlsPreview() {
    ThemePreview {
        PrimaryMockLocationControls(
            mockControlState = MockControlState()
        )
    }
}
