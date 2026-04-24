package com.drew654.mocklocations.domain.model

import android.location.Location
import com.drew654.mocklocations.presentation.mToKm
import com.drew654.mocklocations.presentation.mToMiles
import com.google.android.gms.maps.model.LatLng

data class RouteSegment(
    val points: List<LatLng>
) {
    fun getMapMarkerPoint(): LatLng {
        return points.last()
    }

    fun getDistance(speedUnit: SpeedUnit): Double {
        var totalDistance = 0.0
        val results = FloatArray(1)

        for (i in 0 until points.size - 1) {
            Location.distanceBetween(
                points[i].latitude, points[i].longitude,
                points[i + 1].latitude, points[i + 1].longitude,
                results
            )
            totalDistance += results[0]
        }
        return if (speedUnit is SpeedUnit.MilesPerHour) {
            mToMiles(totalDistance)
        } else {
            mToKm(totalDistance)
        }
    }
}
