package com.drew654.mocklocations.domain.model

import com.google.android.gms.maps.model.LatLng

data class RoutePoint(
    val latLng: LatLng,
    val bearing: Float
)
