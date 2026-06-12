package com.drew654.mocklocations.domain.model

import org.junit.Assert.*
import org.junit.Test

class CompassStateTest {
    @Test
    fun `data class properties are correctly initialized`() {
        val bearing = { 90f }
        val isVisible = { true }

        val compassState = CompassState(
            bearing = bearing,
            isVisible = isVisible
        )

        assertEquals(bearing, compassState.bearing)
        assertEquals(isVisible, compassState.isVisible)
    }
}
