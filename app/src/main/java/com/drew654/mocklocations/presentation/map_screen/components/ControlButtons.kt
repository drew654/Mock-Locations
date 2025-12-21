package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.R
import com.google.android.gms.maps.model.LatLng

@Composable
fun ControlButtons(
    onClearClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    onStopClicked: () -> Unit,
    onPopClicked: () -> Unit,
    points: List<LatLng>,
    isMocking: Boolean
) {
    Column(
        modifier = Modifier.padding(12.dp)
    ) {
        DisableableSmallFloatingActionButton(
            onClick = {
                onPopClicked()
            },
            enabled = points.isNotEmpty() && !isMocking
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_backspace_24),
                contentDescription = "Pop",
                tint = if (points.isEmpty())
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.height(4.dp))
        DisableableSmallFloatingActionButton(
            onClick = {
                onClearClicked()
            },
            enabled = points.isNotEmpty() && !isMocking
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_clear_24),
                contentDescription = "Clear",
                tint = if (points.isEmpty())
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.height(12.dp))
        DisableableFloatingActionButton(
            onClick = {
                if (isMocking) {
                    onStopClicked()
                } else {
                    onPlayClicked()
                }
            },
            enabled = points.isNotEmpty()
        ) {
            if (isMocking) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_stop_24),
                    contentDescription = "Stop"
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                    contentDescription = "Play"
                )
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
