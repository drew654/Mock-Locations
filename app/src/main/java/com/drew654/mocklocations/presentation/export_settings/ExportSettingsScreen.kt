package com.drew654.mocklocations.presentation.export_settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.ExportSettingsState
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import com.drew654.mocklocations.presentation.components.CheckboxRow
import com.drew654.mocklocations.presentation.ui.theme.DayNightDevicePreviews
import com.drew654.mocklocations.presentation.ui.theme.DeviceThemePreview
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ExportSettingsScreen(
    viewModel: MockLocationsViewModel,
    navController: NavController
) {
    val state = viewModel.exportSettingsState.collectAsState()
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.exportDataToUri(it, state.value.isExportSettings, state.value.isExportRoutes)
        }
        navController.popBackStack()
    }

    var isInitialized by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!isInitialized) {
            viewModel.refreshExportSettingsState()
            isInitialized = true
        }
    }

    ExportSettingsContent(
        onBack = {
            navController.popBackStack()
        },
        onExport = {
            val timestamp = SimpleDateFormat(
                "yyyy_MM_dd_HH_mm_ss",
                Locale.getDefault()
            ).format(System.currentTimeMillis())
            exportLauncher.launch("mock_locations_$timestamp.json")
        },
        state = state.value,
        setIsExportSettings = { newValue ->
            viewModel.updateExportSettingsState { it.copy(isExportSettings = newValue) }
        },
        setIsExportRoutes = { newValue ->
            viewModel.updateExportSettingsState { it.copy(isExportRoutes = newValue) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportSettingsContent(
    state: ExportSettingsState,
    onExport: () -> Unit = { },
    onBack: () -> Unit = { },
    setIsExportSettings: (Boolean) -> Unit = { },
    setIsExportRoutes: (Boolean) -> Unit = { }
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.displayCutout.only(
                    WindowInsetsSides.Horizontal
                )
            ),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Export Settings") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onBack()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                            contentDescription = "Back"
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                CheckboxRow(
                    label = "Export settings",
                    checked = state.isExportSettings,
                    onCheckedChange = {
                        setIsExportSettings(it)
                    }
                )
                CheckboxRow(
                    label = "Export ${state.routesToExport} routes",
                    checked = state.isExportRoutes,
                    onCheckedChange = {
                        setIsExportRoutes(it)
                    },
                    enabled = state.routesToExport > 0
                )
                Spacer(Modifier.padding(bottom = 16.dp))
            }

            TextButton(
                onClick = {
                    onExport()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                enabled = state.isExportRoutes || state.isExportSettings
            ) {
                Text("Export")
            }
        }
    }
}

@DayNightDevicePreviews
@Composable
private fun ExportSettingsContentPreview1() {
    DeviceThemePreview {
        ExportSettingsContent(
            ExportSettingsState(
                routesToExport = 5,
                isExportSettings = true,
                isExportRoutes = true
            )
        )
    }
}

@DayNightDevicePreviews
@Composable
private fun ExportSettingsContentPreview2() {
    DeviceThemePreview {
        ExportSettingsContent(
            ExportSettingsState(
                routesToExport = 0,
                isExportSettings = true,
                isExportRoutes = false
            )
        )
    }
}
