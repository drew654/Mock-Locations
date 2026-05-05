package com.drew654.mocklocations.domain.model

import com.google.gson.JsonNull
import com.google.gson.JsonPrimitive
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SpeedUnitTypeAdapterTest {
    private lateinit var adapter: SpeedUnitTypeAdapter

    @Before
    fun setUp() {
        adapter = SpeedUnitTypeAdapter()
    }

    @Test
    fun `serialize returns JsonPrimitive with correct SpeedUnit name`() {
        val mps = SpeedUnit.MetersPerSecond
        val kph = SpeedUnit.KilometersPerHour
        val mph = SpeedUnit.MilesPerHour

        assertEquals("m/s", adapter.serialize(mps, null, null)?.asString)
        assertEquals("km/h", adapter.serialize(kph, null, null)?.asString)
        assertEquals("mph", adapter.serialize(mph, null, null)?.asString)
    }

    @Test
    fun `serialize returns null when source is null`() {
        val result = adapter.serialize(null, null, null)
        assertNull(result)
    }

    @Test
    fun `deserialize returns correct SpeedUnit for valid JSON strings`() {
        assertEquals(SpeedUnit.MetersPerSecond, adapter.deserialize(JsonPrimitive("m/s"), null, null))
        assertEquals(SpeedUnit.KilometersPerHour, adapter.deserialize(JsonPrimitive("km/h"), null, null))
        assertEquals(SpeedUnit.MilesPerHour, adapter.deserialize(JsonPrimitive("mph"), null, null))
    }

    @Test
    fun `deserialize returns null for null or JsonNull input`() {
        assertNull("Should handle null input", adapter.deserialize(null, null, null))
        assertNull("Should handle JsonNull input", adapter.deserialize(JsonNull.INSTANCE, null, null))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `deserialize throws exception for invalid speed unit string`() {
        adapter.deserialize(JsonPrimitive("kts"), null, null)
    }
}
