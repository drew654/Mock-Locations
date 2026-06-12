package com.drew654.mocklocations.domain.model

import org.junit.Assert.*
import org.junit.Test

class ImportRouteOptionTest {
    @Test
    fun `verify all enum constants exist`() {
        val expected = listOf("REPLACE", "MERGE")
        val actual = ImportRouteOption.entries.map { it.name }

        assertEquals(expected.size, actual.size)
        assertTrue(actual.containsAll(expected))
    }

    @Test
    fun `verify all enum labels are correct`() {
        val expected = listOf("Replace current routes", "Merge with current routes")
        val actual = ImportRouteOption.entries.map { it.label }

        assertEquals(expected.size, actual.size)
        assertTrue(actual.containsAll(expected))
    }
}
