package com.drew654.mocklocations.domain.model

sealed class AccuracyLevel(val name: String, val meters: Float) {
    object Perfect : AccuracyLevel("Perfect", 0f)
    object High : AccuracyLevel("High", 5f)
    object Medium : AccuracyLevel("Medium", 10f)
    object Low : AccuracyLevel("Low", 20f)
}

fun getAccuracyLevelByName(name: String): AccuracyLevel? {
    return when (name) {
        AccuracyLevel.Perfect.name -> AccuracyLevel.Perfect
        AccuracyLevel.High.name -> AccuracyLevel.High
        AccuracyLevel.Medium.name -> AccuracyLevel.Medium
        AccuracyLevel.Low.name -> AccuracyLevel.Low
        else -> null
    }
}
