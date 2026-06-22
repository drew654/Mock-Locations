package com.drew654.mocklocations.presentation.import_settings

import android.content.res.Configuration
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.ImportRouteOption
import com.drew654.mocklocations.domain.model.ImportSettingsState
import com.drew654.mocklocations.presentation.components.CheckboxRow
import com.drew654.mocklocations.presentation.components.RadioButtonRow
import com.drew654.mocklocations.presentation.ui.theme.DayNightDevicePreviews
import com.drew654.mocklocations.presentation.ui.theme.DeviceThemePreview
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

@Composable
fun ImportSettingsScreen(
    viewModel: ImportSettingsViewModel = hiltViewModel(),
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.setImportUri(uri)
        } else if (state.importUri == null) {
            navController.popBackStack()
        }
    }

    var hasAutoLaunched by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasAutoLaunched && state.importUri == null) {
            importLauncher.launch(arrayOf("application/json"))
            hasAutoLaunched = true
        }
    }

    ImportSettingsContent(
        state = state,
        onBack = {
            navController.popBackStack()
        },
        onImport = {
            viewModel.importDataFromUri(onSuccess = {
                navController.popBackStack()
            })
        },
        setIsImportRoutes = { newValue ->
            viewModel.setIsImportRoutes(newValue)
        },
        setIsImportSettings = { newValue ->
            viewModel.setIsImportSettings(newValue)
        },
        setImportRouteOption = { newValue ->
            viewModel.setImportRouteOption(newValue)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImportSettingsContent(
    state: ImportSettingsState,
    onImport: () -> Unit = { },
    onBack: () -> Unit = { },
    setIsImportRoutes: (Boolean) -> Unit = { },
    setIsImportSettings: (Boolean) -> Unit = { },
    setImportRouteOption: (ImportRouteOption) -> Unit = { }
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
                title = { Text("Import Settings") },
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
                    label = "Import settings",
                    checked = state.isImportSettings,
                    onCheckedChange = {
                        setIsImportSettings(it)
                    },
                    enabled = state.isImportSettingsEnabled,
                    checkboxTestTag = "import_settings_checkbox"
                )
                CheckboxRow(
                    label = "Import ${state.routesToImport} routes",
                    checked = state.isImportRoutes,
                    onCheckedChange = {
                        setIsImportRoutes(it)
                    },
                    enabled = state.isImportRoutesEnabled,
                    checkboxTestTag = "import_routes_checkbox"
                )
                if (state.isImportRoutes) {
                    RadioButtonRow(
                        label = ImportRouteOption.REPLACE.label,
                        selected = state.importRouteOption == ImportRouteOption.REPLACE,
                        onClick = {
                            setImportRouteOption(ImportRouteOption.REPLACE)
                        },
                        radioButtonTestTag = "replace_routes_radio_button"
                    )
                    RadioButtonRow(
                        label = ImportRouteOption.MERGE.label,
                        selected = state.importRouteOption == ImportRouteOption.MERGE,
                        onClick = {
                            setImportRouteOption(ImportRouteOption.MERGE)
                        },
                        radioButtonTestTag = "merge_routes_radio_button"
                    )
                }
                Spacer(Modifier.padding(bottom = 16.dp))
            }

            TextButton(
                onClick = {
                    onImport()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                enabled = state.isFormValid()
            ) {
                Text(if (state.isImporting) "Importing..." else "Import")
            }
        }
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true
)
@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun ImportSettingsContentPreview1() {
    MockLocationsTheme {
        Surface {
            ImportSettingsContent(
                state = ImportSettingsState(
                    isImportSettingsEnabled = true,
                    isImportSettings = true,
                    isImportRoutesEnabled = true,
                    isImportRoutes = true,
                    routesToImport = 5,
                    importRouteOption = ImportRouteOption.REPLACE
                )
            )
        }
    }
}

@DayNightDevicePreviews
@Composable
private fun ImportSettingsContentPreview2() {
    DeviceThemePreview {
        ImportSettingsContent(
            state = ImportSettingsState(
                isImportSettingsEnabled = false,
                isImportSettings = false,
                isImportRoutesEnabled = true,
                isImportRoutes = true,
                importRouteOption = ImportRouteOption.REPLACE,
                routesToImport = 5
            )
        )
    }
}

@DayNightDevicePreviews
@Composable
private fun ImportSettingsContentPreview3() {
    DeviceThemePreview {
        ImportSettingsContent(
            state = ImportSettingsState(
                isImportSettingsEnabled = true,
                isImportSettings = true,
                isImportRoutesEnabled = false,
                isImportRoutes = false,
                importRouteOption = null,
                routesToImport = 0
            )
        )
    }
}
