package com.drew654.mocklocations.util

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
import com.drew654.mocklocations.domain.model.LocationTarget
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume

object MapUtils {
    suspend fun geocodeAddress(
        context: Context,
        address: String
    ): LatLng? = suspendCancellableCoroutine { continuation ->
        val geocoder = Geocoder(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocationName(address, 1) { results ->
                val latLng = results.firstOrNull()?.let {
                    LatLng(it.latitude, it.longitude)
                }

                continuation.resume(latLng)
            }
        } else {
            try {
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocationName(address, 1)

                val latLng = results?.firstOrNull()?.let {
                    LatLng(it.latitude, it.longitude)
                }

                continuation.resume(latLng)
            } catch (e: IOException) {
                Log.e("MockLocationsViewModel", "Failed to geocode address", e)
                continuation.resume(null)
            }
        }
    }

    fun getMarkerHue(index: Int, numPoints: Int): Float {
        return when (index) {
            0 -> BitmapDescriptorFactory.HUE_GREEN
            numPoints - 1 -> BitmapDescriptorFactory.HUE_RED
            else -> BitmapDescriptorFactory.HUE_YELLOW
        }
    }

    suspend fun focusMapToLocationTarget(
        locationTarget: LocationTarget,
        cameraPositionState: CameraPositionState
    ) {
        if (locationTarget.routeSegments.isEmpty()) return

        val boundsBuilder = LatLngBounds.Builder()
        locationTarget.getAllPoints().forEach { boundsBuilder.include(it) }

        val bounds = boundsBuilder.build()

        val update = if (locationTarget.routeSegments.size == 1) {
            CameraUpdateFactory.newLatLngZoom(locationTarget.getLastPoint()!!, 15f)
        } else {
            CameraUpdateFactory.newLatLngBounds(bounds, 200)
        }

        cameraPositionState.animate(update)
    }
}
