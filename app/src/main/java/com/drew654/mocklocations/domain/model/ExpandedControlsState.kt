package com.drew654.mocklocations.domain.model

data class ExpandedControlsState(
    val isExpanded: Boolean = false,
    val speedSliderLowerEnd: Int = 0,
    val speedSliderUpperEnd: Int = 100,
    val speedUnitValue: SpeedUnitValue = SpeedUnitValue()
)
