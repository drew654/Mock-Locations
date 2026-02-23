package com.drew654.mocklocations.presentation.export_settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import com.drew654.mocklocations.presentation.export_settings.components.ExportSettingsContent
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

@Composable
fun ExportSettingsScreen(
    viewModel: MockLocationsViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingExportSettings by rememberSaveable { mutableStateOf(false) }
    var pendingExportRoutes by rememberSaveable { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        scope.launch {
            val json = viewModel.repository.generateExportToJson(
                exportSettings = pendingExportSettings,
                exportRoutes = pendingExportRoutes
            )

            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(json.toByteArray(Charsets.UTF_8))
                output.flush()
            }
        }
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
                java.util.Locale.getDefault()
            ).format(System.currentTimeMillis())
            exportLauncher.launch("mock_locations_$timestamp.json")
        }
    )
}
