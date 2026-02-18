package com.drew654.mocklocations.presentation.expanded_controls_configuration

import androidx.compose.runtime.Composable
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import com.drew654.mocklocations.presentation.expanded_controls_configuration.components.ExpandedControlsConfigurationContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController

@Composable
fun ExpandedControlsConfigurationScreen(
    viewModel: MockLocationsViewModel,
    navController: NavController
) {
    val speedUnitValue by viewModel.speedUnitValue.collectAsState()
    val speedSliderLowerEnd by viewModel.speedSliderLowerEnd.collectAsState()
    val speedSliderUpperEnd by viewModel.speedSliderUpperEnd.collectAsState()

    ExpandedControlsConfigurationContent(
        originalSpeedUnitValue = speedUnitValue,
        originalSpeedSliderLowerEnd = speedSliderLowerEnd,
        originalSpeedSliderUpperEnd = speedSliderUpperEnd,
        onSaved = { speedUnitValue, speedSliderLowerEnd, speedSliderUpperEnd ->
            if (speedUnitValue.value < speedSliderLowerEnd) {
                viewModel.setSpeedUnitValue(
                    speedUnitValue.copy(value = speedSliderLowerEnd.toDouble())
                )
            } else {
                viewModel.setSpeedUnitValue(speedUnitValue)
            }
            if (speedUnitValue.value > speedSliderUpperEnd) {
                viewModel.setSpeedUnitValue(
                    speedUnitValue.copy(value = speedSliderUpperEnd.toDouble())
                )
            } else {
                viewModel.saveSpeedUnitValue(speedUnitValue)
            }
            viewModel.setSpeedSliderLowerEnd(speedSliderLowerEnd)
            viewModel.setSpeedSliderUpperEnd(speedSliderUpperEnd)
            navController.popBackStack()
        },
        onBack = {
            navController.popBackStack()
        }
    )
}
