package com.drew654.mocklocations.presentation.import_settings.components

import android.content.res.Configuration
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.ImportRouteOption
import com.drew654.mocklocations.presentation.components.CheckboxRow
import com.drew654.mocklocations.presentation.components.RadioButtonRow
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSettingsContent(
    onBack: () -> Unit,
    onImport: (Boolean, ImportRouteOption?) -> Unit,
    isWithSettingsToImport: Boolean,
    routesToImport: Int
) {
    val scrollState = rememberScrollState()
    var isImportingRoutes by rememberSaveable { mutableStateOf(routesToImport > 0) }
    var isImportingSettings by rememberSaveable { mutableStateOf(isWithSettingsToImport) }
    var importRouteOption by rememberSaveable { mutableStateOf<ImportRouteOption?>(ImportRouteOption.REPLACE) }

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
                    checked = isImportingSettings,
                    onCheckedChange = {
                        isImportingSettings = it
                    },
                    enabled = isWithSettingsToImport
                )
                CheckboxRow(
                    label = "Import $routesToImport routes",
                    checked = isImportingRoutes,
                    onCheckedChange = {
                        isImportingRoutes = it
                        importRouteOption =
                            if (isImportingRoutes) ImportRouteOption.REPLACE else null
                    },
                    enabled = isWithSettingsToImport && routesToImport > 0
                )
                if (isImportingRoutes) {
                    ImportRouteOption.entries.forEach { option ->
                        RadioButtonRow(
                            label = option.label,
                            selected = importRouteOption == option,
                            onClick = {
                                importRouteOption = option
                            }
                        )
                    }
                }
                Spacer(Modifier.padding(bottom = 16.dp))
            }

            TextButton(
                onClick = {
                    onImport(isImportingSettings, importRouteOption)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                enabled = isImportingRoutes || isImportingSettings
            ) {
                Text("Import")
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
                onBack = { },
                onImport = { _, _ -> },
                isWithSettingsToImport = true,
                routesToImport = 5
            )
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
private fun ImportSettingsContentPreview2() {
    MockLocationsTheme {
        Surface {
            ImportSettingsContent(
                onBack = { },
                onImport = { _, _ -> },
                isWithSettingsToImport = false,
                routesToImport = 5
            )
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
private fun ImportSettingsContentPreview3() {
    MockLocationsTheme {
        Surface {
            ImportSettingsContent(
                onBack = { },
                onImport = { _, _ -> },
                isWithSettingsToImport = true,
                routesToImport = 0
            )
        }
    }
}
