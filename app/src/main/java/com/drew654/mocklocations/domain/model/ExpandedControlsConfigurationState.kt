package com.drew654.mocklocations.domain.model

data class ExpandedControlsConfigurationState(
    val isShowingDialog: Boolean = false,
    val speedUnitValue: SpeedUnitValue = SpeedUnitValue(),
    val speedSliderLowerEnd: String = "0",
    val speedSliderUpperEnd: String = "100"
) {
    fun formIsValid(): Boolean {
        return !(
                speedSliderLowerEnd.toIntOrNull() == null
                        || speedSliderUpperEnd.toIntOrNull() == null
                        || speedSliderLowerEnd.toInt() >= speedSliderUpperEnd.toInt()
                        || speedSliderLowerEnd.toInt() < 0
                )
    }
}
