package com.drew654.mocklocations.domain.model

import com.google.android.gms.maps.model.LatLng

sealed interface LocationTarget {
    val routeSegments: List<RouteSegment>

    companion object {
        fun create(routeSegments: List<RouteSegment>): LocationTarget {
            return when (routeSegments.size) {
                0 -> Empty
                1 -> SinglePoint(point = routeSegments.first().points.first())
                else -> Route(routeSegments)
            }
        }
    }

    data object Empty : LocationTarget {
        override val routeSegments: List<RouteSegment> = emptyList()
    }

    data class SinglePoint(val point: LatLng) : LocationTarget {
        override val routeSegments: List<RouteSegment> = listOf(RouteSegment(points = listOf(point)))
    }

    data class Route(override val routeSegments: List<RouteSegment>) : LocationTarget {
        fun getDistance(speedUnit: SpeedUnit): Double {
            return routeSegments.sumOf { it.getDistance(speedUnit) }
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
        override val routeSegments: List<RouteSegment>
    ) : LocationTarget {
        fun getDistanceText(speedUnit: SpeedUnit): String = Route(routeSegments).getDistanceText(speedUnit)
    }

    fun isRoute(): Boolean {
        return this is Route || this is SavedRoute
    }

    fun getLastPoint(): LatLng? {
        return when (this) {
            is Empty -> null
            is SinglePoint -> point
            is Route -> routeSegments.lastOrNull()?.points?.lastOrNull()
            is SavedRoute -> routeSegments.lastOrNull()?.points?.lastOrNull()
        }
    }

    fun getAllPoints(): List<LatLng> {
        return when (this) {
            is Empty -> emptyList()
            is SinglePoint -> listOf(point)
            is Route -> routeSegments.flatMap { it.points }
            is SavedRoute -> routeSegments.flatMap { it.points }
        }
    }
}
