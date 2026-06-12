package com.drew654.mocklocations.domain.model

import org.junit.Assert.*
import org.junit.Test

class SpeedUnitValueTest {
    @Test
    fun `toMetersPerSecond delegates correctly for MetersPerSecond`() {
        val speedValue = SpeedUnitValue(10.0, SpeedUnit.MetersPerSecond)
        assertEquals(10.0, speedValue.toMetersPerSecond(), 0.0)
    }

    @Test
    fun `toMetersPerSecond delegates correctly for KilometersPerHour`() {
        val speedValue = SpeedUnitValue(100.0, SpeedUnit.KilometersPerHour)
        assertEquals(27.7778, speedValue.toMetersPerSecond(), 0.000001)
    }

    @Test
    fun `toMetersPerSecond delegates correctly for MilesPerHour`() {
        val speedValue = SpeedUnitValue(60.0, SpeedUnit.MilesPerHour)
        assertEquals(26.8224, speedValue.toMetersPerSecond(), 0.000001)
    }

    @Test
    fun `data class properties are correctly initialized`() {
        val value = 25.5
        val unit = SpeedUnit.MilesPerHour
        val speedValue = SpeedUnitValue(value, unit)

        assertEquals(value, speedValue.value, 0.0)
        assertEquals(unit, speedValue.speedUnit)
    }

    @Test
    fun `copy method works correctly`() {
        val original = SpeedUnitValue(10.0, SpeedUnit.MetersPerSecond)
        val updated = original.copy(value = 20.0)

        assertEquals(20.0, updated.value, 0.0)
        assertEquals(SpeedUnit.MetersPerSecond, updated.speedUnit)
    }

    @Test
    fun `equality and hashCode work correctly`() {
        val s1 = SpeedUnitValue(10.0, SpeedUnit.KilometersPerHour)
        val s2 = SpeedUnitValue(10.0, SpeedUnit.KilometersPerHour)
        val s3 = SpeedUnitValue(15.0, SpeedUnit.KilometersPerHour)

        assertEquals(s1, s2)
        assertEquals(s1.hashCode(), s2.hashCode())
        assertNotEquals(s1, s3)
        assertNotEquals(s1.hashCode(), s3.hashCode())
    }
}
