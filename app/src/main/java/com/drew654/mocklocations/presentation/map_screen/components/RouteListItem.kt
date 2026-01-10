package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme
import com.google.android.gms.maps.model.LatLng

@Composable
fun RouteListItem(
    selected: Boolean,
    shouldShowCheckbox: Boolean,
    route: LocationTarget.SavedRoute,
    onClick: (route: LocationTarget.SavedRoute) -> Unit,
    onLongClick: (route: LocationTarget.SavedRoute) -> Unit
) {
    ListItem(
        headlineContent = { Text(route.name) },
        supportingContent = {
            Text(
                "${route.points.size} points • ${
                    "%.2f".format(
                        route.getDistance() / 1000.0
                    )
                } km"
            )
        },
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = {
                    onClick(route)
                },
                onLongClick = {
                    onLongClick(route)
                }
            ),
        trailingContent = {
            if (shouldShowCheckbox) {
                Checkbox(
                    checked = selected,
                    onCheckedChange = null
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    )
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
fun RouteListItemUnselectedPreview() {
    MockLocationsTheme {
        Surface {
            RouteListItem(
                selected = false,
                route = LocationTarget.SavedRoute(
                    name = "Route 1",
                    points = listOf(
                        LatLng(0.0, 0.0),
                        LatLng(0.0, 0.1)
                    )
                ),
                onClick = { },
                onLongClick = { },
                shouldShowCheckbox = false
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
fun RouteListItemSelectedPreview() {
    MockLocationsTheme {
        Surface {
            RouteListItem(
                selected = true,
                route = LocationTarget.SavedRoute(
                    name = "Route 1",
                    points = listOf(
                        LatLng(0.0, 0.0),
                        LatLng(0.0, 0.1)
                    )
                ),
                onClick = { },
                onLongClick = { },
                shouldShowCheckbox = true
            )
        }
    }
}
