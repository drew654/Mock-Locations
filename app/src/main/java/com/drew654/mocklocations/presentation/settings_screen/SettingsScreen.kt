package com.drew654.mocklocations.presentation.settings_screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import com.drew654.mocklocations.presentation.settings_screen.components.SwitchRow

@Composable
fun SettingsScreen(
    viewModel: MockLocationsViewModel
) {
    val clearRouteOnStop by viewModel.clearRouteOnStop.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        SwitchRow(
            label = "Clear route on stop",
            checked = clearRouteOnStop,
            onCheckedChange = {
                viewModel.setClearRouteOnStop(it)
            }
        )
    }
}
