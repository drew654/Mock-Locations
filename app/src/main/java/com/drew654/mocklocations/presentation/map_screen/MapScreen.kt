package com.drew654.mocklocations.presentation.map_screen

import android.Manifest
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.drew654.mocklocations.domain.model.CompassState
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MapState
import com.drew654.mocklocations.domain.model.Permission
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import com.drew654.mocklocations.domain.model.isGranted
import com.drew654.mocklocations.domain.model.isLongPressAddPointEnabled
import com.drew654.mocklocations.presentation.NoRippleInteractionSource
import com.drew654.mocklocations.presentation.Screen
import com.drew654.mocklocations.presentation.map_screen.components.ExpandedControls
import com.drew654.mocklocations.presentation.map_screen.components.MapControlButtons
import com.drew654.mocklocations.presentation.map_screen.components.PermissionsDialog
import com.drew654.mocklocations.presentation.map_screen.components.SavedRoutesDialog
import com.drew654.mocklocations.presentation.map_screen.components.SearchAddressSection
import com.drew654.mocklocations.presentation.ui.theme.DayNightDevicePreviews
import com.drew654.mocklocations.presentation.ui.theme.DeviceThemePreview
import com.drew654.mocklocations.util.MapUtils
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(isSystemInDarkTheme) {
        viewModel.setIsSystemInDarkTheme(isSystemInDarkTheme)
    }

    LaunchedEffect(Unit) {
        viewModel.setHasLocationPermission(Permission.FineLocation.isGranted(context))
    }
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val locationGranted = result[Permission.FineLocation.permission] ?: false
        val hasLocationPermission = locationGranted || Permission.FineLocation.isGranted(context)
        viewModel.setHasLocationPermission(hasLocationPermission)
        if (!hasLocationPermission) {
            viewModel.setPermissionToBeRequested(Permission.FineLocation)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.setCameraPosition(
                    cameraPositionState.position
                )
            }
            if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_START) {
                if (
                    state.permissionToBeRequested == Permission.MockLocations
                    && Permission.MockLocations.isGranted(context)
                ) {
                    viewModel.setPermissionToBeRequested(null)
                }
                if (
                    state.permissionToBeRequested == Permission.DeveloperOptions
                    && Permission.DeveloperOptions.isGranted(context)
                ) {
                    viewModel.setPermissionToBeRequested(null)
                }
                if (
                    state.permissionToBeRequested == Permission.FineLocation
                    && Permission.FineLocation.isGranted(context)
                ) {
                    viewModel.setPermissionToBeRequested(null)
                    viewModel.setHasLocationPermission(true)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(state.savedCameraPosition) {
        if (!state.hasRestoredCamera && state.savedCameraPosition != null) {
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(
                    state.savedCameraPosition!!.toLatLng(),
                    state.savedCameraPosition!!.zoom
                )
            )
            viewModel.setHasRestoredCamera(true)
        }
    }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow {
            cameraPositionState.isMoving to cameraPositionState.cameraMoveStartedReason
        }.collect { (isMoving, reason) ->
            if (isMoving && reason == CameraMoveStartedReason.GESTURE) {
                viewModel.setIsCameraCurrentlyFollowingMockedLocation(false)
            }

            if (!isMoving) {
                viewModel.setCameraPosition(cameraPositionState.position)
            }
        }
    }

    LaunchedEffect(state.currentMockedLocation, state.isCameraFollowingMockedLocation) {
        if (state.mockControlState.isMocking && state.isCameraCurrentlyFollowingMockedLocation && state.currentMockedLocation != null) {
            cameraPositionState.move(
                CameraUpdateFactory.newLatLng(state.currentMockedLocation!!.latLng)
            )
        }
    }

    LaunchedEffect(state.hasLocationPermission, state.mockControlState.activeLocationTarget) {
        if (!state.isMapCenteredAfterLaunch) {
            if (state.mockControlState.activeLocationTarget !is LocationTarget.Empty) {
                snapshotFlow { cameraPositionState.projection }
                    .filterNotNull()
                    .first()

                try {
                    MapUtils.focusMapToLocationTarget(state.mockControlState.activeLocationTarget, cameraPositionState)
                    viewModel.setMapIsCenteredAfterLaunch()
                } catch (e: Exception) {
                    Log.e("MapScreen", "Error centering map to active location target", e)
                }
            } else if (state.hasLocationPermission) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            cameraPositionState.move(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(location.latitude, location.longitude),
                                    15f
                                )
                            )
                            viewModel.setMapIsCenteredAfterLaunch()
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e("MapScreen", "Error centering map to user", e)
                }
            }
        }
    }

    MapContent(
        state = state,
        onSearchAddress = { address ->
            scope.launch {
                val latLng = MapUtils.geocodeAddress(context, address)
                if (latLng == null) {
                    Toast.makeText(context, "Address not found", Toast.LENGTH_SHORT).show()
                } else {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                    )
                }
            }
        },
        cameraPositionState = cameraPositionState,
        onMapLongClick = { point ->
            if (state.mockControlState.isLongPressAddPointEnabled()) {
                scope.launch {
                    viewModel.pushRouteSegment(point)
                }
            }
        },
        setControlsAreExpanded = {
            viewModel.setControlsAreExpanded(it)
        },
        onClearLocationTarget = {
            viewModel.clearLocationTarget()
        },
        onStart = {
            val permissionsToRequest = buildList {
                if (!Permission.FineLocation.isGranted(context)) {
                    add(Permission.FineLocation.permission)
                }

                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && !Permission.PostNotifications.isGranted(context)
                ) {
                    add(Permission.PostNotifications.permission)
                }
            }

            if (permissionsToRequest.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
                permissionsLauncher.launch(permissionsToRequest.toTypedArray())
                return@MapContent
            }

            if (!Permission.DeveloperOptions.isGranted(context)) {
                viewModel.setPermissionToBeRequested(Permission.DeveloperOptions)
                return@MapContent
            }

            if (!Permission.MockLocations.isGranted(context)) {
                viewModel.setPermissionToBeRequested(Permission.MockLocations)
                return@MapContent
            }

            if (state.isCameraFollowingMockedLocation && state.mockControlState.activeLocationTarget.isRoute()) {
                viewModel.setIsCameraCurrentlyFollowingMockedLocation(true)
                cameraPositionState.move(CameraUpdateFactory.zoomTo(15f))
            }
            scope.launch {
                viewModel.startMockLocation(
                    cameraPositionTarget = cameraPositionState.position.target
                )
            }
        },
        onStop = {
            viewModel.stopMockLocation()
        },
        onPopRouteSegment = {
            viewModel.popRouteSegment()
        },
        onTogglePause = {
            viewModel.togglePause()
        },
        onSaveLocationTarget = {
            viewModel.setIsShowingSavedRoutesDialog(true)
            if (state.mockControlState.isMocking) {
                viewModel.setIsNamingRoute(true)
            }
        },
        onAddCrosshairsPoint = {
            scope.launch {
                viewModel.pushRouteSegment(cameraPositionState.position.target)
            }
        },
        onUserLocationFocus = {
            if (!Permission.FineLocation.isGranted(context)) {
                permissionsLauncher.launch(arrayOf(Permission.FineLocation.permission))
                return@MapContent
            }

            if (state.isCameraFollowingMockedLocation) {
                viewModel.setIsCameraCurrentlyFollowingMockedLocation(true)
                cameraPositionState.move(CameraUpdateFactory.zoomTo(15f))
            }
            scope.launch {
                val fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(context)
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(location.latitude, location.longitude),
                                        15f
                                    )
                                )
                            }
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e("MapScreen", "Failed to get user location", e)
                }
            }
        },
        onShowSearch = { newValue ->
            viewModel.setIsShowingSearch(newValue)
        },
        onSpeedChanged = { newSpeed ->
            viewModel.setSpeedValueUi(newSpeed)
        },
        onSpeedChangeFinished = { speedUnitValue ->
            viewModel.saveSpeedUnitValue(speedUnitValue)
        },
        onSetIsNamingRoute = { newValue ->
            viewModel.setIsNamingRoute(newValue)
        },
        onDismissSavedRouteDialog = {
            viewModel.setIsShowingSavedRoutesDialog(false)
        },
        onRouteSaved = { name ->
            viewModel.saveCurrentRoute(name)
        },
        onRouteLoaded = { savedRoute ->
            viewModel.loadSavedRoute(savedRoute)
            scope.launch {
                MapUtils.focusMapToLocationTarget(savedRoute, cameraPositionState)
            }
        },
        onRouteDeleted = { savedRoute ->
            viewModel.deleteSavedRoute(savedRoute)
        },
        onDismissPermissionsDialog = {
            viewModel.setPermissionToBeRequested(null)
        },
        onClickCompass = {
            scope.launch {
                val currentPos = cameraPositionState.position
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(currentPos.target)
                            .zoom(currentPos.zoom)
                            .bearing(0f)
                            .tilt(0f)
                            .build()
                    )
                )
            }
        },
        compassState = CompassState(
            bearing = { cameraPositionState.position.bearing },
            tilt = { cameraPositionState.position.tilt }
        ),
        onSettingsClick = {
            focusManager.clearFocus()
            navController.navigate(Screen.Settings.route)
        },
        onZoomIn = {
            focusManager.clearFocus()
            scope.launch {
                if (state.isCameraCurrentlyFollowingMockedLocation) {
                    cameraPositionState.move(CameraUpdateFactory.zoomIn())
                } else {
                    cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                }
            }
        },
        onZoomOut = {
            focusManager.clearFocus()
            scope.launch {
                if (state.isCameraCurrentlyFollowingMockedLocation) {
                    cameraPositionState.move(CameraUpdateFactory.zoomOut())
                } else {
                    cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                }
            }
        }
    )
}

@Composable
internal fun MapContent(
    state: MapState,
    cameraPositionState: CameraPositionState,
    compassState: CompassState,
    onSearchAddress: (String) -> Unit = { },
    onMapLongClick: (LatLng) -> Unit = { },
    setControlsAreExpanded: (Boolean) -> Unit = { },
    onClearLocationTarget: () -> Unit = { },
    onStart: () -> Unit = { },
    onStop: () -> Unit = { },
    onPopRouteSegment: () -> Unit = { },
    onTogglePause: () -> Unit = { },
    onSaveLocationTarget: () -> Unit = { },
    onAddCrosshairsPoint: () -> Unit = { },
    onUserLocationFocus: () -> Unit = { },
    onShowSearch: (Boolean) -> Unit = { },
    onSpeedChanged: (Double) -> Unit = { },
    onSpeedChangeFinished: (SpeedUnitValue) -> Unit = { },
    onSetIsNamingRoute: (Boolean) -> Unit = { },
    onDismissSavedRouteDialog: () -> Unit = { },
    onRouteSaved: (String) -> Unit = { },
    onRouteLoaded: (LocationTarget.SavedRoute) -> Unit = { },
    onRouteDeleted: (LocationTarget.SavedRoute) -> Unit = { },
    onDismissPermissionsDialog: () -> Unit = { },
    onClickCompass: () -> Unit = { },
    onSettingsClick: () -> Unit = { },
    onZoomIn: () -> Unit = { },
    onZoomOut: () -> Unit = { }
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val activeLocationTarget = state.mockControlState.activeLocationTarget
    val isMocking = state.mockControlState.isMocking

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = NoRippleInteractionSource(),
                indication = null
            ) {
                focusManager.clearFocus()
            }
    ) {
        Column {
            if (state.isShowingSearch) {
                SearchAddressSection(
                    onSearchAddress = { address ->
                        onSearchAddress(address)
                    },
                    shouldFocusSearchBar = state.shouldFocusSearchBar
                )
            }
            Box(
                modifier = Modifier.weight(1f)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = state.mapProperties,
                    uiSettings = state.mapUiSettings,
                    onMapClick = {
                        focusManager.clearFocus()
                    },
                    onMapLongClick = { point ->
                        focusManager.clearFocus()
                        onMapLongClick(point)
                    }
                ) {
                    if (activeLocationTarget.isRoute()) {
                        Polyline(
                            points = activeLocationTarget.getAllPoints(),
                            color = state.mapStyle?.polyLineStroke ?: MaterialTheme.colorScheme.onBackground,
                            width = 8f * context.resources.displayMetrics.density
                        )
                    }

                    activeLocationTarget.routeSegments.forEachIndexed { index, routeSegment ->
                        Marker(
                            state = MarkerState(position = routeSegment.getMapMarkerPoint()),
                            icon = BitmapDescriptorFactory.defaultMarker(
                                MapUtils.getMarkerHue(
                                    index,
                                    activeLocationTarget.routeSegments.size
                                )
                            ),
                            snippet = "Lat: ${routeSegment.getMapMarkerPoint().latitude}, Lng: ${routeSegment.getMapMarkerPoint().longitude}",
                            title = "Route Point",
                            onClick = {
                                true
                            }
                        )
                    }
                }
                MapControlButtons(
                    mockControlState = state.mockControlState,
                    controlsAreExpanded = state.expandedControlsState.isExpanded,
                    setControlsAreExpanded = {
                        setControlsAreExpanded(it)
                    },
                    onClearLocationTarget = {
                        onClearLocationTarget()
                    },
                    onStart = {
                        onStart()
                    },
                    onStop = {
                        onStop()
                    },
                    onPopRouteSegment = {
                        onPopRouteSegment()
                    },
                    onTogglePause = {
                        onTogglePause()
                    },
                    onSaveLocationTarget = {
                        onSaveLocationTarget()
                    },
                    onAddCrosshairsPoint = {
                        onAddCrosshairsPoint()
                    },
                    onUserLocationFocus = {
                        onUserLocationFocus()
                    },
                    setShowSearch = {
                        onShowSearch(it)
                    },
                    isShowingSearch = state.isShowingSearch,
                    crosshairsColor = state.mapStyle?.polyLineStroke ?: MaterialTheme.colorScheme.onBackground,
                    onClickCompass = {
                        onClickCompass()
                    },
                    compassState = compassState,
                    onSettingsClick = {
                        onSettingsClick()
                    },
                    onZoomIn = {
                        onZoomIn()
                    },
                    onZoomOut = {
                        onZoomOut()
                    }
                )
            }
            ExpandedControls(
                state = state.expandedControlsState,
                onSpeedChanged = {
                    onSpeedChanged(it)
                },
                onSpeedChangeFinished = {
                    onSpeedChangeFinished(it)
                }
            )
        }
    }
    SavedRoutesDialog(
        isVisible = state.isShowingSavedRoutesDialog,
        isNamingRoute = state.isNamingRoute,
        onSetIsNamingRoute = {
            onSetIsNamingRoute(it)
        },
        onDismiss = {
            onDismissSavedRouteDialog()
        },
        savedRoutes = state.savedRoutes,
        onRouteSaved = {
            onRouteSaved(it)
        },
        locationTarget = activeLocationTarget,
        onRouteLoaded = {
            onRouteLoaded(it)
        },
        onRouteDeleted = {
            onRouteDeleted(it)
        },
        isMocking = isMocking,
        speedUnit = state.expandedControlsState.speedUnitValue.speedUnit
    )
    state.permissionToBeRequested?.let { permission ->
        PermissionsDialog(
            permission = permission,
            onDismiss = {
                onDismissPermissionsDialog()
            }
        )
    }
}

@DayNightDevicePreviews
@Composable
private fun MapScreenPreview() {
    DeviceThemePreview {
        MapContent(
            state = MapState(),
            cameraPositionState = CameraPositionState(),
            compassState = CompassState()
        )
    }
}
