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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.CompassState
import com.drew654.mocklocations.domain.model.ExpandedControlsState
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MapStyle
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.Permission
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import com.drew654.mocklocations.domain.model.isGranted
import com.drew654.mocklocations.domain.model.isLongPressAddPointEnabled
import com.drew654.mocklocations.presentation.MockLocationsViewModel
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
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    viewModel: MockLocationsViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val systemInDarkTheme = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val mockControlState by viewModel.mockControlState.collectAsState()
    val activeLocationTarget by remember {
        derivedStateOf { mockControlState.activeLocationTarget }
    }
    val isMocking by remember {
        derivedStateOf { mockControlState.isMocking }
    }
    val isPaused by remember {
        derivedStateOf { mockControlState.isPaused }
    }
    val isUsingCrosshairs by remember {
        derivedStateOf { mockControlState.isUsingCrosshairs }
    }
    var hasLocationPermission by remember {
        mutableStateOf(Permission.FineLocation.isGranted(context))
    }
    var permissionToBeRequested by remember { mutableStateOf<Permission?>(null) }
    val mapStyle by viewModel.mapStyle.collectAsState()
    val mapProperties = MapProperties(
        isMyLocationEnabled = hasLocationPermission,
        isBuildingEnabled = true,
        mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
            context,
            mapStyle?.resourceId
                ?: if (systemInDarkTheme) {
                    R.raw.map_style_night
                } else {
                    R.raw.map_style_standard
                }
        ),
        mapType = mapStyle?.mapType ?: MapType.NORMAL
    )
    val mapUiSettings = MapUiSettings(
        compassEnabled = false,
        myLocationButtonEnabled = false,
        zoomControlsEnabled = false
    )
    var hasRestoredCamera by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var isShowingSavedRoutesDialog by rememberSaveable { mutableStateOf(false) }
    val savedRoutes by viewModel.savedRoutes.collectAsState()
    var isShowingSearch by rememberSaveable { mutableStateOf(false) }
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val locationGranted = result[Permission.FineLocation.permission] ?: false
        hasLocationPermission = locationGranted || Permission.FineLocation.isGranted(context)
        if (!hasLocationPermission) {
            permissionToBeRequested = Permission.FineLocation
        }
    }
    var isNamingRoute by rememberSaveable { mutableStateOf(false) }
    val isCameraFollowingMockedLocation by viewModel.isCameraFollowingMockedLocation.collectAsState()
    val isCameraCurrentlyFollowingMockedLocation by viewModel.isCameraCurrentlyFollowingMockedLocation.collectAsState()
    val currentMockedLocation by viewModel.currentMockedLocation.collectAsState()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.updateCameraPosition(
                    cameraPositionState.position
                )
            }
            if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_START) {
                if (
                    permissionToBeRequested == Permission.MockLocations
                    && Permission.MockLocations.isGranted(context)
                ) {
                    permissionToBeRequested = null
                }
                if (
                    permissionToBeRequested == Permission.DeveloperOptions
                    && Permission.DeveloperOptions.isGranted(context)
                ) {
                    permissionToBeRequested = null
                }
                if (
                    permissionToBeRequested == Permission.FineLocation
                    && Permission.FineLocation.isGranted(context)
                ) {
                    permissionToBeRequested = null
                    hasLocationPermission = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.savedCameraPosition) {
        if (!hasRestoredCamera && uiState.savedCameraPosition != null) {
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(
                    uiState.savedCameraPosition!!.toLatLng(),
                    uiState.savedCameraPosition!!.zoom
                )
            )
            hasRestoredCamera = true
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
                viewModel.updateCameraPosition(cameraPositionState.position)
            }
        }
    }

    LaunchedEffect(currentMockedLocation, isCameraFollowingMockedLocation) {
        if (isMocking && isCameraCurrentlyFollowingMockedLocation && currentMockedLocation != null) {
            cameraPositionState.move(
                CameraUpdateFactory.newLatLng(currentMockedLocation!!.latLng)
            )
        }
    }

    LaunchedEffect(Unit) {
        if (!uiState.isMapCenteredAfterLaunch) {
            if (activeLocationTarget !is LocationTarget.Empty) {
                snapshotFlow { cameraPositionState.projection }
                    .filterNotNull()
                    .first()

                try {
                    MapUtils.focusMapToLocationTarget(activeLocationTarget, cameraPositionState)
                    viewModel.setMapIsCenteredAfterLaunch()
                } catch (e: Exception) {
                    Log.e("MapScreen", "Error centering map to active location target", e)
                }
            } else if (hasLocationPermission) {
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
        isShowingSearch = isShowingSearch,
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
        shouldFocusSearchBar = uiState.shouldFocusSearchBar,
        cameraPositionState = cameraPositionState,
        mapProperties = mapProperties,
        mapUiSettings = mapUiSettings,
        onMapLongClick = { point ->
            if (mockControlState.isLongPressAddPointEnabled()) {
                scope.launch {
                    viewModel.pushRouteSegment(point)
                }
            }
        },
        mapStyle = mapStyle,
        mockControlState = mockControlState,
        expandedControlsState = uiState.expandedControlsState,
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
                permissionToBeRequested = Permission.DeveloperOptions
                return@MapContent
            }

            if (!Permission.MockLocations.isGranted(context)) {
                permissionToBeRequested = Permission.MockLocations
                return@MapContent
            }

            if (isCameraFollowingMockedLocation && activeLocationTarget.isRoute()) {
                viewModel.setIsCameraCurrentlyFollowingMockedLocation(true)
                cameraPositionState.move(CameraUpdateFactory.zoomTo(15f))
            }
            scope.launch {
                viewModel.startMockLocation(
                    context = context,
                    pushPoint = if (isUsingCrosshairs && activeLocationTarget is LocationTarget.Empty) cameraPositionState.position.target else null
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
            isShowingSavedRoutesDialog = true
            if (isMocking) {
                isNamingRoute = true
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

            if (isCameraFollowingMockedLocation) {
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
        onShowSearch = {
            viewModel.setShouldFocusSearchBar(it)
            isShowingSearch = it
        },
        onSpeedChanged = { newSpeed ->
            val oldValue = uiState.expandedControlsState.speedUnitValue
            viewModel.updateExpandedControlsState { it.copy(speedUnitValue = oldValue.copy(value = newSpeed)) }
        },
        onSpeedChangeFinished = {
            viewModel.saveSpeedUnitValue(uiState.expandedControlsState.speedUnitValue)
        },
        isShowingSavedRoutesDialog = isShowingSavedRoutesDialog,
        isNamingRoute = isNamingRoute,
        onSetIsNamingRoute = {
            isNamingRoute = it
        },
        onDismissSavedRouteDialog = {
            isShowingSavedRoutesDialog = false
        },
        savedRoutes = savedRoutes,
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
        permissionToBeRequested = permissionToBeRequested,
        onDismissPermissionsDialog = {
            permissionToBeRequested = null
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
            isVisible = cameraPositionState.position.bearing != 0f || cameraPositionState.position.tilt != 0f,
            bearing = cameraPositionState.position.bearing
        ),
        onSettingsClick = {
            focusManager.clearFocus()
            navController.navigate(Screen.Settings.route)
        },
        onZoomIn = {
            focusManager.clearFocus()
            scope.launch {
                if (isCameraCurrentlyFollowingMockedLocation) {
                    cameraPositionState.move(CameraUpdateFactory.zoomIn())
                } else {
                    cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                }
            }
        },
        onZoomOut = {
            focusManager.clearFocus()
            scope.launch {
                if (isCameraCurrentlyFollowingMockedLocation) {
                    cameraPositionState.move(CameraUpdateFactory.zoomOut())
                } else {
                    cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                }
            }
        }
    )
}

@Composable
private fun MapContent(
    isShowingSearch: Boolean,
    shouldFocusSearchBar: Boolean,
    cameraPositionState: CameraPositionState,
    mapProperties: MapProperties,
    mapUiSettings: MapUiSettings,
    mapStyle: MapStyle?,
    mockControlState: MockControlState,
    expandedControlsState: ExpandedControlsState,
    isShowingSavedRoutesDialog: Boolean,
    isNamingRoute: Boolean,
    savedRoutes: List<LocationTarget.SavedRoute>,
    permissionToBeRequested: Permission?,
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
    val activeLocationTarget = mockControlState.activeLocationTarget
    val isPaused = mockControlState.isPaused
    val isMocking = mockControlState.isMocking

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
            if (isShowingSearch) {
                SearchAddressSection(
                    onSearchAddress = { address ->
                        onSearchAddress(address)
                    },
                    shouldFocusSearchBar = shouldFocusSearchBar
                )
            }
            Box(
                modifier = Modifier.weight(1f)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = mapUiSettings,
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
                            color = mapStyle?.polyLineStroke ?: MaterialTheme.colorScheme.onBackground,
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
                    mockControlState = mockControlState,
                    controlsAreExpanded = expandedControlsState.isExpanded,
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
                    isPaused = isPaused,
                    onAddCrosshairsPoint = {
                        onAddCrosshairsPoint()
                    },
                    onUserLocationFocus = {
                        onUserLocationFocus()
                    },
                    setShowSearch = {
                        onShowSearch(it)
                    },
                    isShowingSearch = isShowingSearch,
                    crosshairsColor = mapStyle?.polyLineStroke ?: MaterialTheme.colorScheme.onBackground,
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
                state = expandedControlsState,
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
        isVisible = isShowingSavedRoutesDialog,
        isNamingRoute = isNamingRoute,
        onSetIsNamingRoute = {
            onSetIsNamingRoute(it)
        },
        onDismiss = {
            onDismissSavedRouteDialog()
        },
        savedRoutes = savedRoutes,
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
        speedUnit = expandedControlsState.speedUnitValue.speedUnit
    )
    permissionToBeRequested?.let { permission ->
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
            isShowingSearch = false,
            shouldFocusSearchBar = true,
            cameraPositionState = CameraPositionState(),
            mapProperties = MapProperties(),
            mapUiSettings = MapUiSettings(),
            mapStyle = null,
            mockControlState = MockControlState(),
            expandedControlsState = ExpandedControlsState(),
            isShowingSavedRoutesDialog = false,
            isNamingRoute = false,
            savedRoutes = emptyList(),
            permissionToBeRequested = null,
            compassState = CompassState(isVisible = true, bearing = 0f)
        )
    }
}
