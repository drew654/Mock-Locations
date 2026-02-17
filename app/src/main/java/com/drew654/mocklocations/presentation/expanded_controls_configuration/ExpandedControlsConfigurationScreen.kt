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
        speedUnitLabel = speedUnitValue.speedUnit.name,
        originalSpeedSliderLowerEnd = speedSliderLowerEnd,
        originalSpeedSliderUpperEnd = speedSliderUpperEnd,
        onSetSpeedSliderLowerEnd = {
            viewModel.setSpeedSliderLowerEnd(it)
        },
        onSetSpeedSliderUpperEnd = {
            viewModel.setSpeedSliderUpperEnd(it)
        },
        onSaved = {
            navController.popBackStack()
        }
    )
}
