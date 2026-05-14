package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.isClearLocationTargetEnabled
import com.drew654.mocklocations.domain.model.isPopPointEnabled
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

@Composable
fun SecondaryMockLocationControls(
    mockControlState: MockControlState,
    onClearLocationTarget: () -> Unit,
    onSaveLocationTarget: () -> Unit,
    onPopRouteSegment: () -> Unit,
    setShowSearch: (Boolean) -> Unit,
    isShowingSearch: Boolean,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
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
            onSaveLocationTarget = onSaveLocationTarget
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
private fun SecondaryMockLocationControlsPreview() {
    MockLocationsTheme {
        Surface {
            SecondaryMockLocationControls(
                mockControlState = MockControlState(),
                onClearLocationTarget = { },
                onSaveLocationTarget = { },
                onPopRouteSegment = { },
                setShowSearch = { },
                isShowingSearch = false,
                scrollState = ScrollState(0)
            )
        }
    }
}
