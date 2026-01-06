package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme
import com.google.android.gms.maps.model.LatLng

@Composable
fun SavedRoutesDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    savedRoutes: List<LocationTarget.SavedRoute>,
    onRouteSaved: (LocationTarget.SavedRoute) -> Unit,
    locationTarget: LocationTarget
) {
    var isNamingRoute by remember { mutableStateOf(false) }
    var routeName by remember { mutableStateOf("") }

    if (isVisible) {
        Dialog(
            onDismissRequest = {
                onDismiss()
                isNamingRoute = false
                routeName = ""
            }
        ) {
            Card {
                if (isNamingRoute) {
                    NamingRouteDialogBody(
                        routeName = routeName,
                        onRouteNameChange = { routeName = it },
                        onBack = {
                            isNamingRoute = false
                            routeName = ""
                        },
                        onConfirm = {
                            onRouteSaved(
                                LocationTarget.SavedRoute(
                                    name = routeName,
                                    points = locationTarget.points
                                )
                            )
                            onDismiss()
                            isNamingRoute = false
                            routeName = ""
                        }
                    )
                } else {
                    RoutesListDialogBody(
                        savedRoutes = savedRoutes,
                        onRouteSaved = {

                        },
                        onDismiss = {
                            onDismiss()
                            isNamingRoute = false
                            routeName = ""
                        },
                        locationTarget = locationTarget,
                        onConfirm = {
                            isNamingRoute = true
                        }
                    )
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
fun SavedRoutesDialogPreview() {
    MockLocationsTheme {
        Surface {
            SavedRoutesDialog(
                isVisible = true,
                onDismiss = {},
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
                onRouteSaved = {},
                locationTarget = LocationTarget.Route(
                    listOf(
                        LatLng(0.0, 0.0),
                        LatLng(0.0, 0.1)
                    )
                )
            )
        }
    }
}
