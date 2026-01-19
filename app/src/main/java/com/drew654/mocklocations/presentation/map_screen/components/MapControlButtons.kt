package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.R
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.launch

@Composable
fun MapControlButtons(
    cameraPositionState: CameraPositionState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.displayCutout.only(
                    WindowInsetsSides.Horizontal
                )
            )
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            SmallFloatingActionButton(
                onClick = {
                    scope.launch {
                        val fusedLocationClient =
                            LocationServices.getFusedLocationProviderClient(context)
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                if (location != null) {
                                    scope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLng(
                                                LatLng(location.latitude, location.longitude)
                                            )
                                        )
                                    }
                                }
                            }
                        } catch (e: SecurityException) {
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_my_location_24),
                    contentDescription = "My Location",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            if (cameraPositionState.position.bearing != 0f) {
                Surface(
                    modifier = Modifier.padding(4.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 2.dp
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val currentPos = cameraPositionState.position
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newCameraPosition(
                                        CameraPosition.Builder()
                                            .target(currentPos.target)
                                            .zoom(currentPos.zoom)
                                            .bearing(0f)
                                            .tilt(currentPos.tilt)
                                            .build()
                                    )
                                )
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_north_24),
                            contentDescription = "Align North",
                            modifier = Modifier.rotate(360f - cameraPositionState.position.bearing),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        MapZoomButtons(
            cameraPositionState = cameraPositionState,
            scope = scope,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .padding(bottom = 32.dp)
        )
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
fun MapControlButtonsPreview() {
    MockLocationsTheme {
        Surface {
            MapControlButtons(
                cameraPositionState = CameraPositionState()
            )
        }
    }
}
