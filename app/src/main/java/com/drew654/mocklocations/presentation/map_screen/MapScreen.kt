package com.drew654.mocklocations.presentation.map_screen

import android.Manifest
import android.os.Build
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
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MockControlAction
import com.drew654.mocklocations.domain.model.Permission
import com.drew654.mocklocations.domain.model.getEnabledActions
import com.drew654.mocklocations.domain.model.getVisibleActions
import com.drew654.mocklocations.domain.model.isGranted
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import com.drew654.mocklocations.presentation.NoRippleInteractionSource
import com.drew654.mocklocations.presentation.map_screen.components.ExpandedControls
import com.drew654.mocklocations.presentation.map_screen.components.MapControlButtons
import com.drew654.mocklocations.presentation.map_screen.components.PermissionsDialog
import com.drew654.mocklocations.presentation.map_screen.components.SavedRoutesDialog
import com.drew654.mocklocations.presentation.map_screen.components.SearchAddressSection
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
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
    val locationTarget = mockControlState.activeLocationTarget
    val isMocking = mockControlState.isMocking
    val isPaused = mockControlState.isPaused
    val isUsingCrosshairs = mockControlState.isUsingCrosshairs
    val speedUnitValue by viewModel.speedUnitValue.collectAsState()
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
    val savedCameraPosition by viewModel.cameraPosition.collectAsState()
    var hasRestoredCamera by remember { mutableStateOf(false) }
    val hasCenteredOnUser by viewModel.hasCenteredOnUser.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var isShowingSavedRoutesDialog by rememberSaveable { mutableStateOf(false) }
    val savedRoutes by viewModel.savedRoutes.collectAsState()
    val controlsAreExpanded by viewModel.controlsAreExpanded.collectAsState()
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
    val speedSliderLowerEnd by viewModel.speedSliderLowerEnd.collectAsState()
    val speedSliderUpperEnd by viewModel.speedSliderUpperEnd.collectAsState()
    val isCameraFollowingMockedLocation by viewModel.isCameraFollowingMockedLocation.collectAsState()
    val isCameraCurrentlyFollowingMockedLocation by viewModel.isCameraCurrentlyFollowingMockedLocation.collectAsState()
    val currentMockedLocation by viewModel.currentMockedLocation.collectAsState()
    val shouldFocusSearchBar by viewModel.shouldFocusSearchBar.collectAsState()

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

    LaunchedEffect(savedCameraPosition) {
        if (!hasRestoredCamera && savedCameraPosition != null) {
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        savedCameraPosition!!.latitude,
                        savedCameraPosition!!.longitude
                    ),
                    savedCameraPosition!!.zoom
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
        if (hasLocationPermission && !hasCenteredOnUser) {
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
                        viewModel.markCenteredOnUser()
                    }
                }
            } catch (_: SecurityException) {
            }
        }
    }

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
                        scope.launch {
                            val latLng = viewModel.geocodeAddress(context, address)
                            if (latLng == null) {
                                Toast.makeText(context, "Address not found", Toast.LENGTH_SHORT).show()
                            } else {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                                )
                            }
                        }
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
                    onMapLongClick = {
                        focusManager.clearFocus()
                        if (MockControlAction.ADD_POINT in mockControlState.getEnabledActions()) {
                            scope.launch {
                                viewModel.pushPoint(it)
                            }
                        }
                    }
                ) {
                    if (locationTarget !is LocationTarget.Empty) {
                        Polyline(
                            points = locationTarget.getAllPoints().map {
                                LatLng(
                                    it.latitude,
                                    it.longitude
                                )
                            },
                            color = mapStyle?.polyLineStroke ?: MaterialTheme.colorScheme.onBackground,
                            width = 8f * context.resources.displayMetrics.density
                        )
                    }

                    locationTarget.routeSegments.forEachIndexed { index, routeSegment ->
                        Marker(
                            state = MarkerState(
                                position = LatLng(
                                    routeSegment.getMapMarkerPoint().latitude,
                                    routeSegment.getMapMarkerPoint().longitude
                                )
                            ),
                            icon = BitmapDescriptorFactory.defaultMarker(
                                getMarkerHue(
                                    index,
                                    locationTarget.routeSegments.size
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
                    navController = navController,
                    visibleMockControlActions = mockControlState.getVisibleActions(),
                    enabledMockControlActions = mockControlState.getEnabledActions(),
                    cameraPositionState = cameraPositionState,
                    controlsAreExpanded = controlsAreExpanded,
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
                            return@MapControlButtons
                        }

                        if (!Permission.DeveloperOptions.isGranted(context)) {
                            permissionToBeRequested = Permission.DeveloperOptions
                            return@MapControlButtons
                        }

                        if (!Permission.MockLocations.isGranted(context)) {
                            permissionToBeRequested = Permission.MockLocations
                            return@MapControlButtons
                        }

                        if (isCameraFollowingMockedLocation && locationTarget.isRoute()) {
                            viewModel.setIsCameraCurrentlyFollowingMockedLocation(true)
                            cameraPositionState.move(CameraUpdateFactory.zoomTo(15f))
                        }
                        scope.launch {
                            viewModel.startMockLocation(
                                context = context,
                                pushPoint = if (isUsingCrosshairs && locationTarget is LocationTarget.Empty) cameraPositionState.position.target else null
                            )
                        }
                    },
                    onStop = {
                        viewModel.stopMockLocation()
                    },
                    onPopPoint = {
                        viewModel.popPoint()
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
                    isPaused = isPaused,
                    onAddCrosshairsPoint = {
                        scope.launch {
                            viewModel.pushPoint(cameraPositionState.position.target)
                        }
                    },
                    onUserLocationFocus = {
                        if (!Permission.FineLocation.isGranted(context)) {
                            permissionsLauncher.launch(arrayOf(Permission.FineLocation.permission))
                            return@MapControlButtons
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
                            } catch (_: SecurityException) {
                            }
                        }
                    },
                    setShowSearch = {
                        viewModel.setShouldFocusSearchBar(it)
                        isShowingSearch = it
                    },
                    isShowingSearch = isShowingSearch,
                    isCameraCurrentlyFollowingMockedLocation = isCameraCurrentlyFollowingMockedLocation,
                    crosshairsColor = mapStyle?.polyLineStroke ?: MaterialTheme.colorScheme.onBackground
                )
            }
            ExpandedControls(
                isExpanded = controlsAreExpanded,
                speedUnitValue = speedUnitValue,
                onSpeedChanged = {
                    viewModel.setSpeedUnitValue(speedUnitValue.copy(value = it))
                },
                onSpeedChangeFinished = {
                    viewModel.saveSpeedUnitValue(speedUnitValue)
                },
                sliderLowerEnd = speedSliderLowerEnd,
                sliderUpperEnd = speedSliderUpperEnd
            )
        }
    }
    SavedRoutesDialog(
        isVisible = isShowingSavedRoutesDialog,
        isNamingRoute = isNamingRoute,
        onSetIsNamingRoute = {
            isNamingRoute = it
        },
        onDismiss = {
            isShowingSavedRoutesDialog = false
        },
        savedRoutes = savedRoutes,
        onRouteSaved = {
            viewModel.saveCurrentRoute(it)
        },
        locationTarget = locationTarget,
        onRouteLoaded = {
            viewModel.loadSavedRoute(it)
            scope.launch {
                focusMapToLocationTarget(it, cameraPositionState)
            }
        },
        onRouteDeleted = {
            viewModel.deleteSavedRoute(it)
        },
        isMocking = isMocking,
        speedUnit = speedUnitValue.speedUnit
    )
    permissionToBeRequested?.let { permission ->
        PermissionsDialog(
            permission = permission,
            setShowMockLocationDialog = { permissionToBeRequested = null },
            onDismiss = {
                permissionToBeRequested = null
            },
            context = context
        )
    }
}

private fun getMarkerHue(index: Int, numPoints: Int): Float {
    return when (index) {
        0 -> BitmapDescriptorFactory.HUE_GREEN
        numPoints - 1 -> BitmapDescriptorFactory.HUE_RED
        else -> BitmapDescriptorFactory.HUE_YELLOW
    }
}

private suspend fun focusMapToLocationTarget(
    locationTarget: LocationTarget,
    cameraPositionState: CameraPositionState
) {
    if (locationTarget.routeSegments.isEmpty()) return

    val boundsBuilder = LatLngBounds.Builder()
    locationTarget.routeSegments.map { it.points }.forEach { points -> points.forEach { boundsBuilder.include(it) } }

    val bounds = boundsBuilder.build()

    val update = if (locationTarget.routeSegments.size == 1) {
        CameraUpdateFactory.newLatLngZoom(locationTarget.getLastPoint()!!, 15f)
    } else {
        CameraUpdateFactory.newLatLngBounds(bounds, 200)
    }

    cameraPositionState.animate(update)
}
