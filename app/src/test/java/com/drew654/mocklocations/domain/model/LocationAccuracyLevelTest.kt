package com.drew654.mocklocations.domain.model

import org.junit.Assert.*
import org.junit.Test

class LocationAccuracyLevelTest {
    @Test
    fun `getLocationAccuracyLevelByName with valid names returns correct level`() {
        assertEquals(LocationAccuracyLevel.Perfect, getLocationAccuracyLevelByName("Perfect"))
        assertEquals(LocationAccuracyLevel.High, getLocationAccuracyLevelByName("High"))
        assertEquals(LocationAccuracyLevel.Medium, getLocationAccuracyLevelByName("Medium"))
        assertEquals(LocationAccuracyLevel.Low, getLocationAccuracyLevelByName("Low"))
    }

    @Test
    fun `getLocationAccuracyLevelByName with invalid name returns null`() {
        assertNull(getLocationAccuracyLevelByName("Invalid"))
        assertNull(getLocationAccuracyLevelByName(""))
        assertNull(getLocationAccuracyLevelByName("perfect"))
    }

    @Test
    fun `LocationAccuracyLevel meters values are correct`() {
        assertEquals(0f, LocationAccuracyLevel.Perfect.meters, 0.0f)
        assertEquals(5f, LocationAccuracyLevel.High.meters, 0.0f)
        assertEquals(10f, LocationAccuracyLevel.Medium.meters, 0.0f)
        assertEquals(20f, LocationAccuracyLevel.Low.meters, 0.0f)
    }
}
