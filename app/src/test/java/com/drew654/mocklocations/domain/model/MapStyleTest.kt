package com.drew654.mocklocations.domain.model

import org.junit.Assert.*
import org.junit.Test

class MapStyleTest {
    @Test
    fun `getMapStyleByName with valid name returns correct style`() {
        assertEquals(MapStyle.Standard, getMapStyleByName("Standard"))
        assertEquals(MapStyle.Night, getMapStyleByName("Night"))
        assertEquals(MapStyle.Satellite, getMapStyleByName("Satellite"))
        assertEquals(MapStyle.Hybrid, getMapStyleByName("Hybrid"))
        assertEquals(MapStyle.Terrain, getMapStyleByName("Terrain"))
        assertEquals(MapStyle.Aubergine, getMapStyleByName("Aubergine"))
        assertEquals(MapStyle.Dark, getMapStyleByName("Dark"))
        assertEquals(MapStyle.Retro, getMapStyleByName("Retro"))
        assertEquals(MapStyle.Silver, getMapStyleByName("Silver"))
    }

    @Test
    fun `getMapStyleByName with invalid name returns null`() {
        assertNull(getMapStyleByName("Invalid"))
        assertNull(getMapStyleByName(""))
        assertNull(getMapStyleByName("standard"))
    }
}
