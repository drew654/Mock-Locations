package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.R
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

@Composable
fun CompassButton(
    onClick: () -> Unit = { },
    bearing: Float
) {
    Surface(
        modifier = Modifier.padding(4.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 2.dp
    ) {
        IconButton(
            onClick = {
                onClick()
            },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_north_24),
                contentDescription = "Align North",
                modifier = Modifier.rotate(360f - bearing),
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
fun CompassButtonPreview() {
    MockLocationsTheme {
        Surface {
            CompassButton(
                bearing = 0f
            )
        }
    }
}
