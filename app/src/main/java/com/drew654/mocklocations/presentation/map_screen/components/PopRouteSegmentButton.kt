package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
fun PopRouteSegmentButton(
    onPopRouteSegment: () -> Unit,
    enabledMockControlActions: Set<MockControlAction>
) {
    DisableableSmallFloatingActionButton(
        onClick = { onPopRouteSegment() },
        enabled = MockControlAction.POP_POINT in enabledMockControlActions
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_backspace_24),
            contentDescription = "Pop",
            tint = if (MockControlAction.POP_POINT in enabledMockControlActions)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurfaceVariant
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
fun PopRouteSegmentButtonPreview() {
    MockLocationsTheme {
        Surface {
            Box(modifier = Modifier.padding(4.dp)) {
                PopRouteSegmentButton(
                    onPopRouteSegment = { },
                    enabledMockControlActions = setOf(
                        MockControlAction.START,
                        MockControlAction.ADD_POINT,
                        MockControlAction.POP_POINT,
                        MockControlAction.CLEAR_LOCATION_TARGET
                    )
                )
            }
        }
    }
}
