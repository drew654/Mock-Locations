package com.drew654.mocklocations.domain.model

data class SpeedUnitValue(
    val value: Double = 30.0,
    val speedUnit: SpeedUnit = SpeedUnit.MilesPerHour
)

fun SpeedUnitValue.toMetersPerSecond(): Double {
    return speedUnit.toMetersPerSecond(value)
}
