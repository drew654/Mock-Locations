package com.drew654.mocklocations.presentation.manage_routes.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.drew654.mocklocations.R
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
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    onClick: () -> Unit = { },
    onEditClicked: () -> Unit = { },
    onCopyClicked: () -> Unit = { },
    onDeleteClicked: () -> Unit = { },
    onUpClicked: () -> Unit = { },
    onDownClicked: () -> Unit = { }
) {
    ListItem(
        headlineContent = { Text(route.name) },
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable {
                onClick()
            },
        supportingContent = {
            Column {
                Text("${route.routeSegments.size} points • ${route.getDistanceText(speedUnit)}")
                AnimatedVisibility(visible = isExpanded) {
                    Row {
                        IconButton(
                            onClick = onEditClicked
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_edit_24),
                                contentDescription = "Edit name"
                            )
                        }
                        IconButton(
                            onClick = onCopyClicked
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_content_copy_24),
                                contentDescription = "Copy"
                            )
                        }
                        IconButton(
                            onClick = onDeleteClicked
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_delete_24),
                                contentDescription = "Delete"
                            )
                        }
                    }
                }
            }
        },
        trailingContent = {
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    IconButton(
                        onClick = onUpClicked
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_upward_24),
                            contentDescription = "Move up"
                        )
                    }
                    IconButton(
                        onClick = onDownClicked
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_downward_24),
                            contentDescription = "Move down"
                        )
                    }
                }
            }
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
