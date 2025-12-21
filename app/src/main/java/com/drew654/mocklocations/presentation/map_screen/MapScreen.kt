package com.drew654.mocklocations.presentation.map_screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.R
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import com.drew654.mocklocations.presentation.map_screen.components.DisableableFloatingActionButton
import com.drew654.mocklocations.presentation.map_screen.components.DisableableSmallFloatingActionButton
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(
    viewModel: MockLocationsViewModel
) {
    val context = LocalContext.current
    val points by viewModel.points.collectAsState()
    var hasLocationPermission by remember { mutableStateOf(false) }
    val isMocking by viewModel.isMocking.collectAsState()
    val clearPointsOnStop by viewModel.clearPointsOnStop.collectAsState()

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
                isMyLocationEnabled = hasLocationPermission
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
            points.forEach { point ->
                Marker(
                    state = MarkerState(
                        position = LatLng(
                            point.latitude,
                            point.longitude
                        )
                    ),
                    title = "Route Point",
                    snippet = "Lat: ${point.latitude}, Lng: ${point.longitude}"
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomStart
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                DisableableSmallFloatingActionButton(
                    onClick = {
                        viewModel.clearPoints()
                    },
                    enabled = points.isNotEmpty() && !isMocking
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_clear_24),
                        contentDescription = "Clear",
                        tint = if (points.isEmpty())
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.height(12.dp))
                DisableableFloatingActionButton(
                    onClick = {
                        if (viewModel.isMocking.value) {
                            viewModel.stopMockLocation()
                            if (clearPointsOnStop) {
                                viewModel.clearPoints()
                            }
                        } else {
                            viewModel.startMockLocation()
                        }
                    },
                    enabled = points.isNotEmpty()
                ) {
                    if (isMocking) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_stop_24),
                            contentDescription = "Stop"
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                            contentDescription = "Play"
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
