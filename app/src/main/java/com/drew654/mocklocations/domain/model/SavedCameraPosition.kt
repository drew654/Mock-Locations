package com.drew654.mocklocations.domain.model

import com.google.android.gms.maps.model.LatLng

data class SavedCameraPosition(
    val latitude: Double,
    val longitude: Double,
    val zoom: Float
) {
    fun toLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }
}
