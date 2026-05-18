package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

@Composable
fun SavedRoutesDialog(
    isVisible: Boolean,
    isNamingRoute: Boolean,
    savedRoutes: List<LocationTarget.SavedRoute>,
    locationTarget: LocationTarget,
    isMocking: Boolean,
    speedUnit: SpeedUnit,
    onSetIsNamingRoute: (Boolean) -> Unit = { },
    onDismiss: () -> Unit = { },
    onRouteSaved: (String) -> Unit = { },
    onRouteLoaded: (LocationTarget.SavedRoute) -> Unit = { },
    onRouteDeleted: (LocationTarget.SavedRoute) -> Unit = { }
) {
    var routeName by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var selectedRoutes by remember { mutableStateOf(emptyList<LocationTarget.SavedRoute>()) }

    if (isVisible) {
        Dialog(
            onDismissRequest = {
                onDismiss()
                onSetIsNamingRoute(false)
                routeName = TextFieldValue("")
            }
        ) {
            Card {
                if (isNamingRoute) {
                    NamingRouteDialogBody(
                        routeName = routeName,
                        onRouteNameChange = { routeName = it },
                        onBack = {
                            if (isMocking) {
                                onDismiss()
                            }
                            onSetIsNamingRoute(false)
                            routeName = TextFieldValue("")
                        },
                        onConfirm = {
                            onRouteSaved(routeName.text)
                            onDismiss()
                            onSetIsNamingRoute(false)
                            routeName = TextFieldValue("")
                        },
                        savedRoutes = savedRoutes
                    )
                } else {
                    RoutesListDialogBody(
                        savedRoutes = savedRoutes,
                        onDismiss = {
                            onDismiss()
                            onSetIsNamingRoute(false)
                            routeName = TextFieldValue("")
                        },
                        onConfirm = {
                            onSetIsNamingRoute(true)
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
                        },
                        speedUnit = speedUnit,
                        isSaveRouteEnabled = locationTarget.isRoute()
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    showSystemUi = true
)
@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun SavedRoutesDialogPreview() {
    MockLocationsTheme {
        Surface {
            SavedRoutesDialog(
                isVisible = true,
                isNamingRoute = false,
                savedRoutes = emptyList(),
                locationTarget = LocationTarget.Empty,
                isMocking = false,
                speedUnit = SpeedUnit.MilesPerHour
            )
        }
    }
}
