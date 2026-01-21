package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onRouteLoaded: (LocationTarget.SavedRoute) -> Unit,
    selectedRoutes: List<LocationTarget.SavedRoute>,
    onRouteSelected: (LocationTarget.SavedRoute) -> Unit,
    onRouteDeselected: (LocationTarget.SavedRoute) -> Unit,
    onClearSelectedRoutes: () -> Unit,
    onDeleteSelectedRoutes: () -> Unit
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
                .weight(1f, fill = false)
                .heightIn(max = 300.dp)
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
                        RouteListItem(
                            selected = selectedRoutes.contains(route),
                            route = route,
                            onClick = {
                                if (selectedRoutes.isEmpty()) {
                                    onRouteLoaded(route)
                                } else {
                                    if (route in selectedRoutes) {
                                        onRouteDeselected(route)
                                    } else {
                                        onRouteSelected(route)
                                    }
                                }
                            },
                            onLongClick = {
                                onRouteSelected(route)
                            },
                            shouldShowCheckbox = selectedRoutes.isNotEmpty()
                        )
                    }
                }
            }
        }
        if (selectedRoutes.isEmpty()) {
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
                    enabled = locationTarget is LocationTarget.Route || locationTarget is LocationTarget.SavedRoute
                ) {
                    Text(text = "Save Route")
                }
            }
        } else {
            Row {
                Spacer(Modifier.weight(1f))
                TextButton(
                    onClick = {
                        onClearSelectedRoutes()
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = "Cancel")
                }
                TextButton(
                    onClick = {
                        onDeleteSelectedRoutes()
                    },
                    modifier = Modifier.padding(8.dp),
                    enabled = selectedRoutes.isNotEmpty()
                ) {
                    Text(text = "Delete Selected")
                }
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
fun RoutesDialogBodyUnselectedPreview() {
    val savedRoutes = listOf(
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
    )
    MockLocationsTheme {
        Surface {
            Card {
                RoutesListDialogBody(
                    savedRoutes = savedRoutes,
                    onDismiss = { },
                    locationTarget = LocationTarget.Route(
                        listOf(
                            LatLng(0.0, 0.0),
                            LatLng(0.0, 0.1)
                        )
                    ),
                    onConfirm = { },
                    onRouteLoaded = { },
                    selectedRoutes = emptyList(),
                    onRouteSelected = { },
                    onRouteDeselected = { },
                    onClearSelectedRoutes = { },
                    onDeleteSelectedRoutes = { }
                )
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
fun RoutesDialogBodySelectedPreview() {
    val savedRoutes = listOf(
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
    )
    MockLocationsTheme {
        Surface {
            Card {
                RoutesListDialogBody(
                    savedRoutes = savedRoutes,
                    onDismiss = { },
                    locationTarget = LocationTarget.Route(
                        listOf(
                            LatLng(0.0, 0.0),
                            LatLng(0.0, 0.1)
                        )
                    ),
                    onConfirm = { },
                    onRouteLoaded = { },
                    selectedRoutes = listOf(savedRoutes[0]),
                    onRouteSelected = { },
                    onRouteDeselected = { },
                    onClearSelectedRoutes = { },
                    onDeleteSelectedRoutes = { }
                )
            }
        }
    }
}
