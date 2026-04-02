package com.drew654.mocklocations.presentation.export_settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import com.drew654.mocklocations.presentation.export_settings.components.ExportSettingsContent
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ExportSettingsScreen(
    viewModel: MockLocationsViewModel,
    navController: NavController
) {
    val savedRoutes by viewModel.savedRoutes.collectAsState()
    var pendingExportSettings by rememberSaveable { mutableStateOf(false) }
    var pendingExportRoutes by rememberSaveable { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.exportDataToUri(it, pendingExportSettings, pendingExportRoutes)
        }
        navController.popBackStack()
    }

    ExportSettingsContent(
        onBack = {
            navController.popBackStack()
        },
        onExport = { isExportingSettings, isExportingRoutes ->
            pendingExportSettings = isExportingSettings
            pendingExportRoutes = isExportingRoutes

            val timestamp = SimpleDateFormat(
                "yyyy_MM_dd_HH_mm_ss",
                Locale.getDefault()
            ).format(System.currentTimeMillis())
            exportLauncher.launch("mock_locations_$timestamp.json")
        },
        routesToExport = savedRoutes.size
    )
}
