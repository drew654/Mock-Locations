package com.drew654.mocklocations.domain.model

sealed class LocationAccuracyLevel(val name: String, val meters: Float) {
    object Perfect : LocationAccuracyLevel("Perfect", 0f)
    object High : LocationAccuracyLevel("High", 5f)
    object Medium : LocationAccuracyLevel("Medium", 10f)
    object Low : LocationAccuracyLevel("Low", 20f)
}

fun getLocationAccuracyLevelByName(name: String): LocationAccuracyLevel? {
    return when (name) {
        LocationAccuracyLevel.Perfect.name -> LocationAccuracyLevel.Perfect
        LocationAccuracyLevel.High.name -> LocationAccuracyLevel.High
        LocationAccuracyLevel.Medium.name -> LocationAccuracyLevel.Medium
        LocationAccuracyLevel.Low.name -> LocationAccuracyLevel.Low
        else -> null
    }
}
