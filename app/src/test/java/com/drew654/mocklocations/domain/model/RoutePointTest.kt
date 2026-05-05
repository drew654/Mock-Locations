package com.drew654.mocklocations.domain.model

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test

class RoutePointTest {
    @Test
    fun `data class properties are correctly initialized`() {
        val latLng = LatLng(30.615075809996384,-96.34175263345242)
        val bearing = 235.72516f
        val routePoint = RoutePoint(latLng, bearing)

        assertEquals(latLng, routePoint.latLng)
        assertEquals(bearing, routePoint.bearing)
    }
}
