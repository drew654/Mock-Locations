package com.drew654.mocklocations.domain.model

import org.junit.Assert.*
import org.junit.Test

class SpeedUnitTest {
    @Test
    fun `getSpeedUnitByName returns correct SpeedUnit for valid names`() {
        assertEquals(SpeedUnit.MetersPerSecond, getSpeedUnitByName("m/s"))
        assertEquals(SpeedUnit.KilometersPerHour, getSpeedUnitByName("km/h"))
        assertEquals(SpeedUnit.MilesPerHour, getSpeedUnitByName("mph"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getSpeedUnitByName throws exception for invalid name`() {
        getSpeedUnitByName("invalid")
    }

    @Test
    fun `toMetersPerSecond converts MetersPerSecond correctly`() {
        val unit = SpeedUnit.MetersPerSecond
        val speed = 10.0
        assertEquals(10.0, unit.toMetersPerSecond(speed), 0.000001)
    }

    @Test
    fun `toMetersPerSecond converts KilometersPerHour correctly`() {
        val unit = SpeedUnit.KilometersPerHour
        val speed = 100.0
        assertEquals(27.7778, unit.toMetersPerSecond(speed), 0.000001)
    }

    @Test
    fun `toMetersPerSecond converts MilesPerHour correctly`() {
        val unit = SpeedUnit.MilesPerHour
        val speed = 60.0
        assertEquals(26.8224, unit.toMetersPerSecond(speed), 0.000001)
    }

    @Test
    fun `toMetersPerSecond handles zero speed`() {
        assertEquals(0.0, SpeedUnit.MetersPerSecond.toMetersPerSecond(0.0), 0.0)
        assertEquals(0.0, SpeedUnit.KilometersPerHour.toMetersPerSecond(0.0), 0.0)
        assertEquals(0.0, SpeedUnit.MilesPerHour.toMetersPerSecond(0.0), 0.0)
    }
}
