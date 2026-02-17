package com.drew654.mocklocations.domain.model

sealed class SpeedUnit(val name: String) {
    object MetersPerSecond : SpeedUnit("m/s")
    object KilometersPerHour : SpeedUnit("km/h")
    object MilesPerHour : SpeedUnit("mph")
}

fun SpeedUnit.toMetersPerSecond(speed: Double): Double {
    return when (this) {
        SpeedUnit.MetersPerSecond -> speed
        SpeedUnit.KilometersPerHour -> speed * 0.277778
        SpeedUnit.MilesPerHour -> speed * 0.44704
    }
}
