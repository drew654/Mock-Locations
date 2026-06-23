package com.drew654.mocklocations.presentation.manage_routes.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.RouteSegment
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.drew654.mocklocations.presentation.ui.theme.DayNightPreviews
import com.drew654.mocklocations.presentation.ui.theme.ThemePreview
import com.google.android.gms.maps.model.LatLng

@Composable
fun RouteListItem(
    route: LocationTarget.SavedRoute,
    speedUnit: SpeedUnit,
    onClick: () -> Unit = { }
) {
    ListItem(
        headlineContent = { Text(route.name) },
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable {
                onClick()
            },
        supportingContent = {
            Text("${route.routeSegments.size} points • ${route.getDistanceText(speedUnit)}")
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    )
}

@DayNightPreviews
@Composable
private fun RouteListItemPreview() {
    ThemePreview {
        RouteListItem(
            route = LocationTarget.SavedRoute(
                name = "Route 1",
                routeSegments = listOf(
                    RouteSegment(
                        points = listOf(
                            LatLng(0.0, 0.0),
                            LatLng(0.0, 0.1)
                        )
                    )
                )
            ),
            speedUnit = SpeedUnit.MilesPerHour
        )
    }
}
