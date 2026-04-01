package com.drew654.mocklocations.presentation.import_settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import com.drew654.mocklocations.presentation.import_settings.components.ImportSettingsContent

@Composable
fun ImportSettingsScreen(
    viewModel: MockLocationsViewModel,
    navController: NavController
) {
    ImportSettingsContent(
        onBack = {
            navController.popBackStack()
            viewModel.setImportUri(null)
        },
        onImport = { importSettings, importRouteOption ->
            viewModel.importDataFromUri(importSettings = importSettings, importRouteOption = importRouteOption)
            navController.popBackStack()
        }
    )
}
