package com.drew654.mocklocations.domain.legacy.v12

import android.location.Location
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.drew654.mocklocations.presentation.mToKm
import com.drew654.mocklocations.presentation.mToMiles
import com.google.android.gms.maps.model.LatLng

sealed interface LegacyLocationTarget12 {
    val points: List<LatLng>

    companion object {
        fun create(points: List<LatLng>): LegacyLocationTarget12 {
            return when (points.size) {
                0 -> Empty
                1 -> SinglePoint(points.first())
                else -> Route(points)
            }
        }
    }

    data object Empty : LegacyLocationTarget12 {
        override val points: List<LatLng> = emptyList()
    }

    data class SinglePoint(val point: LatLng) : LegacyLocationTarget12 {
        override val points: List<LatLng> = listOf(point)
    }

    data class Route(override val points: List<LatLng>) : LegacyLocationTarget12 {
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

        fun getDistanceText(speedUnit: SpeedUnit): String {
            val distance = getDistance(speedUnit)
            return if (speedUnit is SpeedUnit.MilesPerHour) {
                "${"%.2f".format(distance)} mi"
            } else {
                "${"%.2f".format(distance)} km"
            }
        }
    }

    data class SavedRoute(
        val name: String,
        override val points: List<LatLng>
    ) : LegacyLocationTarget12 {
        fun getDistanceText(speedUnit: SpeedUnit): String = Route(points).getDistanceText(speedUnit)
    }

    fun isRoute(): Boolean {
        return this is Route || this is SavedRoute
    }
}
