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
fun ResumeMockingButton(
    onTogglePause: () -> Unit,
    enabledMockControlActions: Set<MockControlAction>,
    modifier: Modifier = Modifier
) {
    DisableableSmallFloatingActionButton(
        onClick = { onTogglePause() },
        enabled = MockControlAction.RESUME in enabledMockControlActions,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_play_arrow_24),
            contentDescription = "Resume",
            tint = MaterialTheme.colorScheme.onPrimaryContainer
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
fun ResumeMockingButtonPreview() {
    MockLocationsTheme {
        Surface {
            Box(modifier = Modifier.padding(4.dp)) {
                ResumeMockingButton(
                    onTogglePause = { },
                    enabledMockControlActions = setOf(
                        MockControlAction.STOP,
                        MockControlAction.RESUME
                    )
                )
            }
        }
    }
}
