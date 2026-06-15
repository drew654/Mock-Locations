package com.drew654.mocklocations.domain.model

import org.junit.Assert.*
import org.junit.Test

class CompassStateTest {
    @Test
    fun `data class properties are correctly initialized`() {
        val bearing = { 90f }
        val tilt = { 60f }

        val compassState = CompassState(
            bearing = bearing,
            tilt = tilt
        )

        assertEquals(bearing, compassState.bearing)
        assertEquals(tilt, compassState.tilt)
    }

    @Test
    fun `isVisible returns true when bearing is not 0f`() {
        val compassState = CompassState(
            bearing = { 90f }
        )

        assertTrue(compassState.isVisible())
    }

    @Test
    fun `isVisible returns true when tilt is not 0f`() {
        val compassState = CompassState(
            tilt = { 60f }
        )

        assertTrue(compassState.isVisible())
    }

    @Test
    fun `isVisible returns false when bearing and tilt are 0f`() {
        val compassState = CompassState()

        assertFalse(compassState.isVisible())
    }
}
