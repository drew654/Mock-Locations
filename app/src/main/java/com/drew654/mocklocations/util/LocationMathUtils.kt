package com.drew654.mocklocations.util

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object LocationMathUtils {
    fun getUpdatedNoiseLatLng(
        accuracyMeters: Float,
        noiseLat: Double,
        noiseLng: Double
    ): Pair<Double, Double> {
        val earthRadius = 6371000.0

        val noiseMeters = accuracyMeters * (0.3 + Random.nextDouble() * 0.4)
        val randomDistance = Math.random() * noiseMeters
        val randomAngle = Math.random() * 2 * Math.PI

        val dLat = (randomDistance * cos(randomAngle)) / earthRadius
        val dLng = (randomDistance * sin(randomAngle)) / earthRadius

        val randomLat = Math.toDegrees(dLat)
        val randomLng = Math.toDegrees(dLng)

        val alpha = (accuracyMeters / 50f).coerceIn(0.05f, 0.4f)

        val newNoiseLat = noiseLat * (1 - alpha) + randomLat * alpha
        val newNoiseLng = noiseLng * (1 - alpha) + randomLng * alpha
        
        return Pair(newNoiseLat, newNoiseLng)
    }

    fun interpolate(start: LatLng, end: LatLng, fraction: Double): LatLng {
        val lat = start.latitude + (end.latitude - start.latitude) * fraction
        val lng = start.longitude + (end.longitude - start.longitude) * fraction
        return LatLng(lat, lng)
    }

    fun findProgressOnRoute(
        anchorPoints: List<LatLng>,
        restorePoint: LatLng
    ): Pair<Int, Double> {
        var closestSegmentIndex = 0
        var closestDistanceInSegment = 0.0
        var minTotalDistanceToLine = Double.MAX_VALUE

        for (i in 0 until anchorPoints.size - 1) {
            val p1 = anchorPoints[i]
            val p2 = anchorPoints[i + 1]

            val results = FloatArray(3)
            Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results)
            val segmentLength = results[0].toDouble()

            Location.distanceBetween(
                p1.latitude,
                p1.longitude,
                restorePoint.latitude,
                restorePoint.longitude,
                results
            )
            val distanceToStart = results[0].toDouble()

            Location.distanceBetween(
                p2.latitude,
                p2.longitude,
                restorePoint.latitude,
                restorePoint.longitude,
                results
            )
            val distanceToEnd = results[0].toDouble()

            val deviation = (distanceToStart + distanceToEnd) - segmentLength

            if (deviation < minTotalDistanceToLine) {
                minTotalDistanceToLine = deviation
                closestSegmentIndex = i
                closestDistanceInSegment = distanceToStart
            }
        }

        return Pair(closestSegmentIndex, closestDistanceInSegment)
    }
}
