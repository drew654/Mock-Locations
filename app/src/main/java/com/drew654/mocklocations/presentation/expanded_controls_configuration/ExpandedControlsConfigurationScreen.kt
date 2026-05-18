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
    val uiState by viewModel.uiState.collectAsState()

    ExpandedControlsConfigurationContent(
        originalState = uiState.expandedControlsState,
        onSaved = { expandedControlsState ->
            val speedUnitValue = expandedControlsState.speedUnitValue
            val speedSliderLowerEnd = expandedControlsState.speedSliderLowerEnd
            val speedSliderUpperEnd = expandedControlsState.speedSliderUpperEnd
            viewModel.setSpeedUnitValue(speedUnitValue)
            viewModel.saveSpeedUnitValue(speedUnitValue)
            if (speedUnitValue.value < speedSliderLowerEnd) {
                viewModel.setSpeedUnitValue(speedUnitValue.copy(value = speedSliderLowerEnd.toDouble()))
                viewModel.saveSpeedUnitValue(speedUnitValue.copy(value = speedSliderLowerEnd.toDouble()))
            }
            if (speedUnitValue.value > speedSliderUpperEnd) {
                viewModel.setSpeedUnitValue(speedUnitValue.copy(value = speedSliderUpperEnd.toDouble()))
                viewModel.saveSpeedUnitValue(speedUnitValue.copy(value = speedSliderUpperEnd.toDouble()))
            }
            viewModel.setSpeedSliderLowerEnd(speedSliderLowerEnd)
            viewModel.saveSpeedSliderLowerEnd(speedSliderLowerEnd)
            viewModel.setSpeedSliderUpperEnd(speedSliderUpperEnd)
            viewModel.saveSpeedSliderUpperEnd(speedSliderUpperEnd)
            navController.popBackStack()
        },
        onBack = {
            navController.popBackStack()
        }
    )
}
