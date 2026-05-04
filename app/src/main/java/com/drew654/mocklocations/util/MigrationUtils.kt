package com.drew654.mocklocations.util

import com.drew654.mocklocations.domain.legacy.v14.LegacyLocationTarget14
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.RouteSegment

object MigrationUtils {
    val gson = JsonUtils.gson

    fun migrateSavedRouteTo15(route: LegacyLocationTarget14.SavedRoute): LocationTarget.SavedRoute {
        return LocationTarget.SavedRoute(
            name = route.name,
            routeSegments = route.points.map { point ->
                RouteSegment(
                    points = listOf(point)
                )
            }
        )
    }

    fun migrateSavedRoutesJsonTo15(routesJson: String): String {
        val legacyRoutes = gson.fromJson(routesJson, Array<LegacyLocationTarget14.SavedRoute>::class.java)
        val newRoutes = legacyRoutes.map { migrateSavedRouteTo15(it) }
        return gson.toJson(newRoutes)
    }
}
