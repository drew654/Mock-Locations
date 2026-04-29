package com.drew654.mocklocations.util

import com.drew654.mocklocations.domain.legacy.v12.LegacyLocationTarget12
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.RouteSegment

object MigrationUtils {
    val gson = JsonUtils.gson

    fun migrateSavedRouteTo13(route: LegacyLocationTarget12.SavedRoute): LocationTarget.SavedRoute {
        return LocationTarget.SavedRoute(
            name = route.name,
            routeSegments = route.points.map { point ->
                RouteSegment(
                    points = listOf(point)
                )
            }
        )
    }

    fun migrateSavedRoutesJsonTo13(routesJson: String): String {
        val legacyRoutes = gson.fromJson(routesJson, Array<LegacyLocationTarget12.SavedRoute>::class.java)
        val newRoutes = legacyRoutes.map { migrateSavedRouteTo13(it) }
        return gson.toJson(newRoutes)
    }
}
