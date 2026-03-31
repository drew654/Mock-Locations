package com.drew654.mocklocations.presentation.settings_screen

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.drew654.mocklocations.R
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import com.drew654.mocklocations.presentation.Screen
import com.drew654.mocklocations.presentation.settings_screen.components.AccuracyLevelDialog
import com.drew654.mocklocations.presentation.settings_screen.components.LocationUpdateDelayDialog
import com.drew654.mocklocations.presentation.settings_screen.components.MapStyleDialog
import com.drew654.mocklocations.presentation.settings_screen.components.ResetSettingsDialog
import com.drew654.mocklocations.presentation.settings_screen.components.SwitchRow
import com.drew654.mocklocations.presentation.settings_screen.components.TextRow
import com.drew654.mocklocations.presentation.toTrimmedString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MockLocationsViewModel,
    navController: NavController
) {
    val mockControlState by viewModel.mockControlState.collectAsState()
    val isUsingCrosshairs = mockControlState.isUsingCrosshairs
    val clearPointsOnStop by viewModel.clearRouteOnStop.collectAsState()
    var isShowingMapStylesDialog by remember { mutableStateOf(false) }
    var isShowingAccuracyLevelDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val mapStyle by viewModel.mapStyle.collectAsState()
    val accuracyLevel by viewModel.accuracyLevel.collectAsState()
    val locationUpdateDelay by viewModel.locationUpdateDelay.collectAsState()
    var isShowingLocationUpdateDelayDialog by remember { mutableStateOf(false) }
    val isCameraFollowingMockedLocation by viewModel.isCameraFollowingMockedLocation.collectAsState()
    val isGoingToWaitAtRouteFinish by viewModel.isGoingToWaitAtRouteFinish.collectAsState()
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importDataFromUri(it)
        }
    }
    var isShowingResetSettingsDialog by remember { mutableStateOf(false) }

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
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
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
                .verticalScroll(scrollState)
        ) {
            SwitchRow(
                label = "Use crosshairs",
                checked = isUsingCrosshairs,
                onCheckedChange = {
                    viewModel.setIsUsingCrosshairs(it)
                }
            )
            SwitchRow(
                label = "Clear route on stop",
                checked = clearPointsOnStop,
                onCheckedChange = {
                    viewModel.setClearRouteOnStop(it)
                }
            )
            SwitchRow(
                label = "Camera follows mocked location",
                checked = isCameraFollowingMockedLocation,
                onCheckedChange = {
                    viewModel.setIsCameraFollowingMockedLocation(it)
                    viewModel.setIsCameraCurrentlyFollowingMockedLocation(it)
                }
            )
            SwitchRow(
                label = "Wait at the end of a route",
                checked = isGoingToWaitAtRouteFinish,
                onCheckedChange = {
                    viewModel.setIsGoingToWaitAtRouteFinish(it)
                }
            )
            TextRow(
                label = "Map style",
                onClick = {
                    isShowingMapStylesDialog = true
                },
                value = mapStyle?.name ?: "Default"
            )
            TextRow(
                label = "Accuracy level",
                onClick = {
                    isShowingAccuracyLevelDialog = true
                },
                value = accuracyLevel.name
            )
            TextRow(
                label = "Location update delay",
                onClick = {
                    isShowingLocationUpdateDelayDialog = true
                },
                value = "${locationUpdateDelay.toTrimmedString()} s"
            )
            TextRow(
                label = "Configure expanded controls",
                onClick = {
                    navController.navigate(Screen.ExpandedControlsConfiguration.route)
                }
            )
            TextRow(
                label = "Export settings",
                onClick = {
                    navController.navigate(Screen.ExportSettings.route)
                }
            )
            TextRow(
                label = "Import settings",
                onClick = {
                    importLauncher.launch(arrayOf("application/json"))
                }
            )
            TextRow(
                label = "Reset to default",
                onClick = {
                    isShowingResetSettingsDialog = true
                }
            )
            TextRow(
                label = "Manual",
                onClick = {
                    Intent().apply {
                        action = Intent.ACTION_VIEW
                        data = "https://github.com/drew654/Mock-Locations/blob/master/README.md".toUri()
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        navController.context.startActivity(this)
                    }
                }
            )
            TextRow(
                label = "Privacy policy",
                onClick = {
                    Intent().apply {
                        action = Intent.ACTION_VIEW
                        data = "https://github.com/drew654/Mock-Locations/blob/master/PRIVACY_POLICY.md".toUri()
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        navController.context.startActivity(this)
                    }
                }
            )
        }
    }

    MapStyleDialog(
        isVisible = isShowingMapStylesDialog,
        onDismiss = { isShowingMapStylesDialog = false },
        selectedStyle = mapStyle,
        onStyleSelected = {
            viewModel.setMapStyle(it)
            isShowingMapStylesDialog = false
        }
    )

    AccuracyLevelDialog(
        isVisible = isShowingAccuracyLevelDialog,
        onDismiss = { isShowingAccuracyLevelDialog = false },
        selectedLevel = accuracyLevel,
        onLevelSelected = {
            viewModel.setAccuracyLevel(it)
            isShowingAccuracyLevelDialog = false
        }
    )

    LocationUpdateDelayDialog(
        isVisible = isShowingLocationUpdateDelayDialog,
        onDismiss = { isShowingLocationUpdateDelayDialog = false },
        locationUpdateDelay = locationUpdateDelay,
        onLocationUpdateDelayChanged = {
            viewModel.setLocationUpdateDelay(it)
        }
    )

    ResetSettingsDialog(
        isVisible = isShowingResetSettingsDialog,
        onConfirm = {
            viewModel.resetSettingsToDefault()
            isShowingResetSettingsDialog = false
        },
        onDismiss = {
            isShowingResetSettingsDialog = false
        }
    )
}
