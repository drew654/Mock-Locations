package com.drew654.mocklocations.presentation.map_screen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import com.drew654.mocklocations.presentation.hasFineLocationPermission
import com.drew654.mocklocations.presentation.map_screen.components.ControlButtons
import com.drew654.mocklocations.presentation.map_screen.components.SavedRoutesDialog
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(
    viewModel: MockLocationsViewModel
) {
    val context = LocalContext.current
    val systemInDarkTheme = isSystemInDarkTheme()
    val locationTarget by viewModel.locationTarget.collectAsState()
    val isMocking by viewModel.isMocking.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val speedMetersPerSec by viewModel.speedMetersPerSec.collectAsState()
    var hasLocationPermission by remember {
        mutableStateOf(hasFineLocationPermission(context))
    }
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
    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                myLocationButtonEnabled = true
            )
        )
    }
    val cameraPositionState = rememberCameraPositionState {
        val zoom = if (locationTarget !is LocationTarget.Empty) 15f else 1f
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), zoom)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    var isShowingSavedRoutesDialog by remember { mutableStateOf(false) }
    val savedRoutes by viewModel.savedRoutes.collectAsState()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasLocationPermission = hasFineLocationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && locationTarget is LocationTarget.Empty) {
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
                    }
                }
            } catch (e: SecurityException) {
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings,
            onMapLongClick = {
                if (!isMocking) {
                    viewModel.pushPoint(it)
                }
            }
        ) {
            if (locationTarget !is LocationTarget.Empty) {
                Polyline(
                    points = locationTarget.points.map { LatLng(it.latitude, it.longitude) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.displayCutout),
            contentAlignment = Alignment.BottomStart
        ) {
            ControlButtons(
                onClearClicked = {
                    viewModel.clearLocationTarget()
                },
                onPlayClicked = {
                    if (isPaused) {
                        viewModel.togglePause()
                    } else {
                        viewModel.startMockLocation(context)
                    }
                },
                onStopClicked = {
                    viewModel.stopMockLocation()
                },
                onPopClicked = {
                    viewModel.popPoint()
                },
                onPauseClicked = {
                    viewModel.togglePause()
                },
                speedMetersPerSec = speedMetersPerSec,
                onSpeedChanged = {
                    viewModel.setSpeedMetersPerSec(it)
                },
                onSaveClicked = {
                    isShowingSavedRoutesDialog = true
                },
                locationTarget = locationTarget,
                isMocking = isMocking,
                isPaused = isPaused
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
        },
        onRouteDeleted = {
            viewModel.deleteSavedRoute(it)
        }
    )
}

private fun getMarkerHue(index: Int, numPoints: Int): Float {
    return when (index) {
        0 -> BitmapDescriptorFactory.HUE_GREEN
        numPoints - 1 -> BitmapDescriptorFactory.HUE_RED
        else -> BitmapDescriptorFactory.HUE_YELLOW
    }
}
