package com.drew654.mocklocations.domain.model

data class ExportSettings(
    val useCrosshairs: Boolean,
    val clearRouteOnStop: Boolean,
    val cameraFollowsMockedLocation: Boolean,
    val mapStyle: String?,
    val expandedControlsSpeedUnitValue: SpeedUnitValue,
    val expandedControlsSpeedSliderLowerEnd: Int,
    val expandedControlsSpeedSliderUpperEnd: Int
)
