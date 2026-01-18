package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.R
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

@Composable
fun ExpandControlsButton(
    onClick: () -> Unit,
    controlsAreExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.outline_chevron_right_24),
                contentDescription = "Toggle Controls",
                modifier = Modifier.rotate(90f + if (controlsAreExpanded) 0f else 180f),
                tint = MaterialTheme.colorScheme.onSurface
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
fun ExpandControlsButtonPreview() {
    MockLocationsTheme {
        Surface {
            ExpandControlsButton(
                onClick = {},
                controlsAreExpanded = false
            )
        }
    }
}
