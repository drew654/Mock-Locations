package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.isClearLocationTargetEnabled
import com.drew654.mocklocations.domain.model.isPopPointEnabled
import com.drew654.mocklocations.presentation.ui.theme.DayNightPreviews
import com.drew654.mocklocations.presentation.ui.theme.ThemePreview

@Composable
fun SecondaryMockLocationControls(
    mockControlState: MockControlState,
    isShowingSearch: Boolean,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    onClearLocationTarget: () -> Unit = { },
    onSaveLocationTarget: () -> Unit = { },
    onPopRouteSegment: () -> Unit = { },
    setShowSearch: (Boolean) -> Unit = { }
) {
    Column(
        modifier = modifier
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
        SearchAddressButton(
            setShowSearch = setShowSearch,
            isShowingSearch = isShowingSearch
        )
        Spacer(Modifier.height(4.dp))
        SavedRoutesButton(
            onClick = onSaveLocationTarget
        )
        Spacer(Modifier.height(4.dp))
        ClearLocationTargetButton(
            onClearLocationTarget = onClearLocationTarget,
            enabled = mockControlState.isClearLocationTargetEnabled()
        )
        Spacer(Modifier.height(4.dp))
        PopRouteSegmentButton(
            onPopRouteSegment = onPopRouteSegment,
            enabled = mockControlState.isPopPointEnabled()
        )
    }
}

@DayNightPreviews
@Composable
private fun SecondaryMockLocationControlsPreview() {
    ThemePreview {
        SecondaryMockLocationControls(
            mockControlState = MockControlState(),
            isShowingSearch = false,
            scrollState = ScrollState(0)
        )
    }
}
