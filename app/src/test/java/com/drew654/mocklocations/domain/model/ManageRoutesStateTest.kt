package com.drew654.mocklocations.domain.model

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test

class ManageRoutesStateTest {
    @Test
    fun `default values are correct`() {
        val state = ManageRoutesState()
        
        assertTrue(state.routes.isEmpty())
        assertEquals(SpeedUnit.MilesPerHour, state.speedUnit)
        assertNull(state.selectedIndex)
    }

    @Test
    fun `data class properties are correctly initialized`() {
        val routes = listOf(
            LocationTarget.SavedRoute(
                "Route 1",
                listOf(RouteSegment(listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0))))
            )
        )
        val speedUnit = SpeedUnit.KilometersPerHour
        val selectedIndex = 5
        
        val state = ManageRoutesState(
            routes = routes,
            speedUnit = speedUnit,
            selectedIndex = selectedIndex
        )
        
        assertEquals(routes, state.routes)
        assertEquals(speedUnit, state.speedUnit)
        assertEquals(selectedIndex, state.selectedIndex)
    }
}
