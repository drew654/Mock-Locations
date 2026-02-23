package com.drew654.mocklocations.presentation.export_settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.drew654.mocklocations.presentation.export_settings.components.ExportSettingsContent

@Composable
fun ExportSettingsScreen(
    navController: NavController
) {
    ExportSettingsContent(
        onBack = {
            navController.popBackStack()
        }
    )
}
