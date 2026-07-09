package com.drew654.mocklocations.domain.model

data class ManageRoutesState(
    val routes: List<LocationTarget.SavedRoute> = emptyList(),
    val speedUnit: SpeedUnit = SpeedUnit.MilesPerHour,
    val selectedRouteName: String? = null
)
