package com.drew654.mocklocations.presentation.map_screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.drew654.mocklocations.R
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import com.drew654.mocklocations.presentation.map_screen.components.ControlButtons
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
    val points by viewModel.points.collectAsState()
    var hasLocationPermission by remember { mutableStateOf(false) }
    val isMocking by viewModel.isMocking.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val clearPointsOnStop by viewModel.clearPointsOnStop.collectAsState()
    val speedMetersPerSec by viewModel.speedMetersPerSec.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
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
        val zoom = if (points.isNotEmpty()) 15f else 1f
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), zoom)
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && points.isEmpty()) {
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
                viewModel.pushPoint(it)
            }
        ) {
            if (points.isNotEmpty()) {
                Polyline(
                    points = points.map { LatLng(it.latitude, it.longitude) },
                    color = MaterialTheme.colorScheme.onBackground,
                    width = 20f
                )
            }

            points.forEachIndexed { index, point ->
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
                            points.size
                        )
                    ),
                    snippet = "Lat: ${point.latitude}, Lng: ${point.longitude}",
                    title = "Route Point"
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomStart
        ) {
            ControlButtons(
                onClearClicked = {
                    viewModel.clearPoints()
                },
                onPlayClicked = {
                    if (isPaused) {
                        viewModel.togglePause()
                    } else {
                        viewModel.startMockLocation()
                    }
                },
                onStopClicked = {
                    viewModel.stopMockLocation()
                    if (clearPointsOnStop) {
                        viewModel.clearPoints()
                    }
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
                points = points,
                isMocking = isMocking,
                isPaused = isPaused
            )
        }
    }
}

private fun getMarkerHue(index: Int, numPoints: Int): Float {
    return when (index) {
        0 -> BitmapDescriptorFactory.HUE_GREEN
        numPoints - 1 -> BitmapDescriptorFactory.HUE_RED
        else -> BitmapDescriptorFactory.HUE_YELLOW
    }
}
