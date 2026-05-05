package com.drew654.mocklocations.domain.model

import org.junit.Assert.*
import org.junit.Test

class MockControlActionTest {
    @Test
    fun `verify all enum constants exist`() {
        val expectedActions = listOf("START", "STOP", "PAUSE", "RESUME", "ADD_POINT", "POP_POINT", "CLEAR_LOCATION_TARGET")

        val actualActions = MockControlAction.entries.map { it.name }

        assertEquals(expectedActions.size, actualActions.size)
        assertTrue(actualActions.containsAll(expectedActions))
    }
}
