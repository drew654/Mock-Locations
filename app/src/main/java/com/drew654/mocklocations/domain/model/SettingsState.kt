package com.drew654.mocklocations.domain.model

data class SettingsState(
    val isBuildRouteOnRoads: Boolean = false,
    val isUsingCrosshairs: Boolean = true,
    val clearPointsOnStop: Boolean = false,
    val isCameraFollowingMockedLocation: Boolean = true,
    val isGoingToWaitAtRouteFinish: Boolean = false,
    val mapStyle: MapStyle? = null,
    val locationAccuracyLevel: LocationAccuracyLevel = LocationAccuracyLevel.Perfect,
    val locationUpdateDelay: Float = 1f,
    val isShowingMapStyleDialog: Boolean = false,
    val isShowingLocationAccuracyLevelDialog: Boolean = false,
    val isShowingLocationUpdateDelayDialog: Boolean = false,
    val isShowingResetSettingsDialog: Boolean = false
)
