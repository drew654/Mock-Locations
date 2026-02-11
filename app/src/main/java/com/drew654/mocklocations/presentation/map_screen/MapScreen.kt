package com.drew654.mocklocations.presentation.map_screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.Permission
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import com.drew654.mocklocations.presentation.components.PermissionsDialog
import com.drew654.mocklocations.presentation.hasFineLocationPermission
import com.drew654.mocklocations.presentation.hasNotificationPermission
import com.drew654.mocklocations.presentation.isAppSetAsMockLocationsApp
import com.drew654.mocklocations.presentation.isDeveloperOptionsEnabled
import com.drew654.mocklocations.presentation.map_screen.components.ExpandedControls
import com.drew654.mocklocations.presentation.map_screen.components.MapControlButtons
import com.drew654.mocklocations.presentation.map_screen.components.SavedRoutesDialog
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
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
    val systemInDarkTheme = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val locationTarget by viewModel.activeLocationTarget.collectAsState()
    val isMocking by viewModel.isMocking.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val speedMetersPerSec by viewModel.speedMetersPerSec.collectAsState()
    var hasLocationPermission by remember {
        mutableStateOf(hasFineLocationPermission(context))
    }
    var permissionToBeRequested by remember { mutableStateOf<Permission?>(null) }
    var isShowingPermissionDialog by remember { mutableStateOf(false) }
    val mapProperties by remember(hasLocationPermission) {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                    context,
                    if (systemInDarkTheme) R.raw.map_style_night else R.raw.map_style_standard
                )
            )
        )
    }
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
    var isShowingSavedRoutesDialog by remember { mutableStateOf(false) }
    val savedRoutes by viewModel.savedRoutes.collectAsState()
    val controlsAreExpanded by viewModel.controlsAreExpanded.collectAsState()
    val isUsingCrosshairs by viewModel.isUsingCrosshairs.collectAsState()
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val locationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        hasLocationPermission = locationGranted || hasFineLocationPermission(context)
        if (!hasLocationPermission) {
            permissionToBeRequested = Permission.FineLocation
            isShowingPermissionDialog = true
        }
    }


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
                    && isAppSetAsMockLocationsApp(context)
                ) {
                    isShowingPermissionDialog = false
                }
                if (
                    permissionToBeRequested == Permission.DeveloperOptions
                    && isDeveloperOptionsEnabled(context)
                ) {
                    isShowingPermissionDialog = false
                }
                if (
                    permissionToBeRequested == Permission.FineLocation
                    && hasFineLocationPermission(context)
                ) {
                    isShowingPermissionDialog = false
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
            cameraPositionState.isMoving to cameraPositionState.position
        }.collect { (isMoving, position) ->
            if (!isMoving) {
                viewModel.updateCameraPosition(position)
            }
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = mapUiSettings,
                    onMapLongClick = {
                        if (!isMocking) {
                            scope.launch {
                                viewModel.pushPoint(it)
                            }
                        }
                    }
                ) {
                    if (locationTarget !is LocationTarget.Empty) {
                        Polyline(
                            points = locationTarget.points.map {
                                LatLng(
                                    it.latitude,
                                    it.longitude
                                )
                            },
                            color = MaterialTheme.colorScheme.onBackground,
                            width = 20f
                        )
                    }

                    locationTarget.points.forEachIndexed { index, point ->
                        Marker(
                            state = MarkerState(
                                position = LatLng(
                                    point.latitude,
                                    point.longitude
                                )
                            ),
                            icon = BitmapDescriptorFactory.defaultMarker(
                                getMarkerHue(
                                    index,
                                    locationTarget.points.size
                                )
                            ),
                            snippet = "Lat: ${point.latitude}, Lng: ${point.longitude}",
                            title = "Route Point",
                            onClick = {
                                true
                            }
                        )
                    }
                }
                MapControlButtons(
                    navController = navController,
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
                            if (!hasFineLocationPermission(context)) {
                                add(Manifest.permission.ACCESS_FINE_LOCATION)
                            }

                            if (
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                                && !hasNotificationPermission(context)
                            ) {
                                add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }

                        if (permissionsToRequest.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
                            return@MapControlButtons
                        }

                        if (!isDeveloperOptionsEnabled(context)) {
                            permissionToBeRequested = Permission.DeveloperOptions
                            isShowingPermissionDialog = true
                            return@MapControlButtons
                        }

                        if (!isAppSetAsMockLocationsApp(context)) {
                            permissionToBeRequested = Permission.MockLocations
                            isShowingPermissionDialog = true
                            return@MapControlButtons
                        }

                        scope.launch {
                            if (isUsingCrosshairs && locationTarget is LocationTarget.Empty) {
                                viewModel.pushPoint(cameraPositionState.position.target)
                            }
                            viewModel.startMockLocation(context)
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
                    },
                    locationTarget = locationTarget,
                    isMocking = isMocking,
                    isPaused = isPaused,
                    isUsingCrosshairs = isUsingCrosshairs,
                    onAddCrosshairsPoint = {
                        scope.launch {
                            viewModel.pushPoint(cameraPositionState.position.target)
                        }
                    }
                )
            }
            ExpandedControls(
                isExpanded = controlsAreExpanded,
                speedMetersPerSec = speedMetersPerSec,
                onSpeedChanged = {
                    viewModel.setSpeedMetersPerSec(it)
                },
                onSpeedChangeFinished = {
                    viewModel.saveSpeedMetersPerSec(speedMetersPerSec)
                }
            )
        }
    }
    SavedRoutesDialog(
        isVisible = isShowingSavedRoutesDialog,
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
        }
    )
    permissionToBeRequested?.let { permission ->
        PermissionsDialog(
            permission = permission,
            showMockLocationDialog = isShowingPermissionDialog,
            setShowMockLocationDialog = { isShowingPermissionDialog = it },
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
    if (locationTarget.points.isEmpty()) return

    val boundsBuilder = LatLngBounds.Builder()
    locationTarget.points.forEach { boundsBuilder.include(it) }

    val bounds = boundsBuilder.build()

    val update = if (locationTarget.points.size == 1) {
        CameraUpdateFactory.newLatLngZoom(locationTarget.points.first(), 15f)
    } else {
        CameraUpdateFactory.newLatLngBounds(bounds, 200)
    }

    cameraPositionState.animate(update)
}
