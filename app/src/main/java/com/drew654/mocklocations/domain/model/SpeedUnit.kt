package com.drew654.mocklocations.domain.model

sealed class SpeedUnit(val name: String) {
    object MetersPerSecond : SpeedUnit("m/s")
    object KilometersPerHour : SpeedUnit("km/h")
    object MilesPerHour : SpeedUnit("mph")
}

fun getSpeedUnitByName(name: String): SpeedUnit {
    return when (name) {
        SpeedUnit.MetersPerSecond.name -> SpeedUnit.MetersPerSecond
        SpeedUnit.KilometersPerHour.name -> SpeedUnit.KilometersPerHour
        else -> SpeedUnit.MilesPerHour
    }
}
