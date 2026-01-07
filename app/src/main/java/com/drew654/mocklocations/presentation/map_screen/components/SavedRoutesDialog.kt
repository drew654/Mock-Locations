package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import com.drew654.mocklocations.domain.model.LocationTarget

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
