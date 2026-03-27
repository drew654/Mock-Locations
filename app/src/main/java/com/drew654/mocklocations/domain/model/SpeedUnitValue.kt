package com.drew654.mocklocations.domain.model

data class SpeedUnitValue(val value: Double, val speedUnit: SpeedUnit)

fun SpeedUnitValue.toMetersPerSecond(): Double {
    return speedUnit.toMetersPerSecond(value)
}
