package com.drew654.mocklocations.util

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
import com.google.android.gms.maps.model.LatLng
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
}
