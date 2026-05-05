package com.drew654.mocklocations.domain.model

import org.junit.Assert.*
import org.junit.Test

class SavedCameraPositionTest {
    @Test
    fun `data class properties are correctly initialized`() {
        val latitude = 30.615165834851403
        val longitude = -96.34165674448013
        val zoom = 15f
        val savedCameraPosition = SavedCameraPosition(latitude, longitude, zoom)

        assertEquals(latitude, savedCameraPosition.latitude, 0.0)
        assertEquals(longitude, savedCameraPosition.longitude, 0.0)
        assertEquals(zoom, savedCameraPosition.zoom, 0f)
    }
}
