package com.drew654.mocklocations.domain.model

import com.google.android.gms.maps.model.LatLng

data class RouteSegment(
    val points: List<LatLng>
) {
    fun getMapMarkerPoint(): LatLng {
        return points.last()
    }
}
