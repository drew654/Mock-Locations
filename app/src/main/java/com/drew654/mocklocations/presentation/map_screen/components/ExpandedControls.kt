package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

@Composable
fun ExpandedControls(
    isExpanded: Boolean,
    speedMetersPerSec: Double,
    onSpeedChanged: (Double) -> Unit,
    onSpeedChangeFinished: (Double) -> Unit
) {
    if (isExpanded) {
        Row(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(16.dp)
                .padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(116.dp)
            ) {
                Text(
                    text = "${speedMetersPerSec.toInt()} m/s",
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Slider(
                value = speedMetersPerSec.toFloat(),
                onValueChange = {
                    onSpeedChanged(it.toDouble())
                },
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .fillMaxWidth()
                ,
                onValueChangeFinished = {
                    onSpeedChangeFinished(speedMetersPerSec)
                },
                valueRange = 0f..100f
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
fun ExpandedControlsPreview() {
    MockLocationsTheme {
        Surface {
            ExpandedControls(
                isExpanded = true,
                speedMetersPerSec = 30.0,
                onSpeedChanged = { },
                onSpeedChangeFinished = { }
            )
        }
    }
}
