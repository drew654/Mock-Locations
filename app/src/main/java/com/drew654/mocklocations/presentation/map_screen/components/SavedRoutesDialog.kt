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

@Composable
fun SavedRoutesDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    savedRoutes: List<LocationTarget.SavedRoute>,
    onRouteSaved: (String) -> Unit,
    locationTarget: LocationTarget,
    onRouteLoaded: (LocationTarget.SavedRoute) -> Unit,
    onRouteDeleted: (LocationTarget.SavedRoute) -> Unit
) {
    var isNamingRoute by remember { mutableStateOf(false) }
    var routeName by remember { mutableStateOf("") }
    var selectedRoutes by remember { mutableStateOf(emptyList<LocationTarget.SavedRoute>()) }

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
                            onRouteSaved(routeName)
                            onDismiss()
                            isNamingRoute = false
                            routeName = ""
                        },
                        savedRoutes = savedRoutes
                    )
                } else {
                    RoutesListDialogBody(
                        savedRoutes = savedRoutes,
                        onDismiss = {
                            onDismiss()
                            isNamingRoute = false
                            routeName = ""
                        },
                        locationTarget = locationTarget,
                        onConfirm = {
                            isNamingRoute = true
                        },
                        onRouteLoaded = {
                            onRouteLoaded(it)
                            onDismiss()
                        },
                        selectedRoutes = selectedRoutes,
                        onRouteSelected = {
                            if (!selectedRoutes.contains(it)) {
                                selectedRoutes = selectedRoutes + it
                            }
                        },
                        onRouteDeselected = {
                            selectedRoutes = selectedRoutes - it
                        },
                        onClearSelectedRoutes = {
                            selectedRoutes = emptyList()
                        },
                        onDeleteSelectedRoutes = {
                            selectedRoutes.forEach { route ->
                                onRouteDeleted(route)
                            }
                            selectedRoutes = emptyList()
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
                onDismiss = { },
                savedRoutes = emptyList(),
                onRouteSaved = { },
                locationTarget = LocationTarget.Empty,
                onRouteLoaded = { },
                onRouteDeleted = { }
            )
        }
    }
}
