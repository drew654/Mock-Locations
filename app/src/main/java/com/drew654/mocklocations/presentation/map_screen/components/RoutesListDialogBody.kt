package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme
import com.google.android.gms.maps.model.LatLng

@Composable
fun RoutesListDialogBody(
    savedRoutes: List<LocationTarget.SavedRoute>,
    onDismiss: () -> Unit,
    locationTarget: LocationTarget,
    onConfirm: () -> Unit,
    onRouteSelected: (LocationTarget.SavedRoute) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Saved Routes",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        Box(
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
        ) {
            if (savedRoutes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No saved routes found")
                }
            } else {
                LazyColumn {
                    items(savedRoutes) { route ->
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
                                .clickable {
                                    onRouteSelected(route)
                                },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
            }
        }
        Row {
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = {
                    onDismiss()
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Cancel")
            }
            TextButton(
                onClick = {
                    onConfirm()
                },
                modifier = Modifier.padding(8.dp),
                enabled = locationTarget is LocationTarget.Route
            ) {
                Text(text = "Save Route")
            }
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
fun RoutesDialogBodyPreview() {
    MockLocationsTheme {
        Surface {
            Card {
                RoutesListDialogBody(
                    savedRoutes = listOf(
                        LocationTarget.SavedRoute(
                            name = "Route 1",
                            points = listOf(
                                LatLng(0.0, 0.0),
                                LatLng(0.0, 0.1)
                            )
                        ),
                        LocationTarget.SavedRoute(
                            name = "Route 2",
                            points = listOf(
                                LatLng(0.0, 0.0),
                                LatLng(0.0, 0.12),
                                LatLng(0.0, 0.08)
                            )
                        )
                    ),
                    onDismiss = { },
                    locationTarget = LocationTarget.Route(
                        listOf(
                            LatLng(0.0, 0.0),
                            LatLng(0.0, 0.1)
                        )
                    ),
                    onConfirm = { },
                    onRouteSelected = { }
                )
            }
        }
    }
}
