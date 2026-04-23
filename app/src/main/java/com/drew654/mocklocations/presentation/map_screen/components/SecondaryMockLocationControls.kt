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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.MockControlAction
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

@Composable
fun SecondaryMockLocationControls(
    enabledMockControlActions: Set<MockControlAction>,
    onClearLocationTarget: () -> Unit,
    onSaveLocationTarget: () -> Unit,
    onPopPoint: () -> Unit,
    setShowSearch: (Boolean) -> Unit,
    isShowingSearch: Boolean,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    val enabledTint = MaterialTheme.colorScheme.onPrimaryContainer
    val disabledTint = MaterialTheme.colorScheme.onSurfaceVariant

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
        DisableableSmallFloatingActionButton(
            onClick = {
                setShowSearch(!isShowingSearch)
            },
            enabled = true
        ) {
            if (isShowingSearch) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_search_off_24),
                    contentDescription = "Close search",
                    tint = enabledTint
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_search_24),
                    contentDescription = "Search",
                    tint = enabledTint
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        DisableableSmallFloatingActionButton(
            onClick = {
                onSaveLocationTarget()
            },
            enabled = true
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_save_24),
                contentDescription = "Saved Routes",
                tint = enabledTint
            )
        }
        Spacer(Modifier.height(4.dp))
        DisableableSmallFloatingActionButton(
            onClick = {
                onClearLocationTarget()
            },
            enabled = MockControlAction.CLEAR_LOCATION_TARGET in enabledMockControlActions
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_clear_24),
                contentDescription = "Clear",
                tint = if (MockControlAction.CLEAR_LOCATION_TARGET in enabledMockControlActions)
                    enabledTint
                else
                    disabledTint
            )
        }
        Spacer(Modifier.height(4.dp))
        DisableableSmallFloatingActionButton(
            onClick = {
                onPopPoint()
            },
            enabled = MockControlAction.POP_POINT in enabledMockControlActions
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_backspace_24),
                contentDescription = "Pop",
                tint = if (MockControlAction.POP_POINT in enabledMockControlActions)
                    enabledTint
                else
                    disabledTint
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
private fun SecondaryMockLocationControlsPreview() {
    MockLocationsTheme {
        Surface {
            SecondaryMockLocationControls(
                enabledMockControlActions = setOf(
                    MockControlAction.STOP,
                    MockControlAction.PAUSE
                ),
                onClearLocationTarget = { },
                onSaveLocationTarget = { },
                onPopPoint = { },
                setShowSearch = { },
                isShowingSearch = false,
                scrollState = ScrollState(0)
            )
        }
    }
}
