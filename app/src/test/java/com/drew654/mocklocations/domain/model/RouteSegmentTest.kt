package com.drew654.mocklocations.domain.model

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test

class RouteSegmentTest {
    @Test
    fun `getMapMarkerPoint returns the last point in the segment`() {
        val routeSegment = RouteSegment(
            points = listOf(
                LatLng(30.614729, -96.342341),
                LatLng(30.614746, -96.342355),
                LatLng(30.614704, -96.342415),
                LatLng(30.614523, -96.34268)
            )
        )

        assertEquals(routeSegment.getMapMarkerPoint(), LatLng(30.614523, -96.34268))
    }
}
