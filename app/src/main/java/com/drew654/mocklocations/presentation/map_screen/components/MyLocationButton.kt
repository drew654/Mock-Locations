package com.drew654.mocklocations.presentation.map_screen.components

import android.content.Context
import android.content.res.Configuration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.drew654.mocklocations.R
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MyLocationButton(
    scope: CoroutineScope,
    context: Context,
    cameraPositionState: CameraPositionState
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
fun MyLocationButtonPreview() {
    MockLocationsTheme {
        Surface {
            MyLocationButton(
                scope = CoroutineScope(Dispatchers.Main),
                context = LocalContext.current,
                cameraPositionState = CameraPositionState()
            )
        }
    }
}
