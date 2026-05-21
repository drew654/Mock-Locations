package com.drew654.mocklocations.presentation.settings_screen

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.drew654.mocklocations.BuildConfig
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.LocationAccuracyLevel
import com.drew654.mocklocations.domain.model.MapStyle
import com.drew654.mocklocations.domain.model.SettingsState
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import com.drew654.mocklocations.presentation.Screen
import com.drew654.mocklocations.presentation.settings_screen.components.LocationAccuracyLevelDialog
import com.drew654.mocklocations.presentation.settings_screen.components.LocationUpdateDelayDialog
import com.drew654.mocklocations.presentation.settings_screen.components.MapStyleDialog
import com.drew654.mocklocations.presentation.settings_screen.components.ResetSettingsDialog
import com.drew654.mocklocations.presentation.settings_screen.components.SwitchRow
import com.drew654.mocklocations.presentation.settings_screen.components.TextRow
import com.drew654.mocklocations.presentation.toTrimmedString
import com.drew654.mocklocations.presentation.ui.theme.DayNightDevicePreviews
import com.drew654.mocklocations.presentation.ui.theme.DeviceThemePreview

@Composable
fun SettingsScreen(
    viewModel: MockLocationsViewModel,
    navController: NavController
) {
    val state = viewModel.settingsState.collectAsState()
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.setImportUri(it)
            val versionCode = viewModel.getVersionCodeFromUri()
            if (versionCode > BuildConfig.VERSION_CODE) {
                Toast.makeText(navController.context, "App version is out of date", Toast.LENGTH_SHORT).show()
                viewModel.setImportUri(null)
                return@let
            }
            navController.navigate(Screen.ImportSettings.route)
        }
    }

    var isInitialized by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!isInitialized) {
            viewModel.refreshSettingsState()
            isInitialized = true
        }
    }

    SettingsContent(
        state = state.value,
        onBack = {
            viewModel.setShouldFocusSearchBar(false)
            navController.popBackStack()
        },
        setBuildRouteOnRoad = { newValue ->
            viewModel.updateSettingsState { it.copy(isBuildRouteOnRoads = newValue) }
            viewModel.setBuildRouteOnRoads(newValue)
        },
        setIsUsingCrosshairs = { newValue ->
            viewModel.updateSettingsState { it.copy(isUsingCrosshairs = newValue) }
            viewModel.setIsUsingCrosshairs(newValue)
        },
        setClearRouteOnStop = { newValue ->
            viewModel.updateSettingsState { it.copy(clearPointsOnStop = newValue) }
            viewModel.setClearRouteOnStop(newValue)
        },
        setIsCameraFollowingMockedLocation = { newValue ->
            viewModel.updateSettingsState { it.copy(isCameraFollowingMockedLocation = newValue) }
            viewModel.setIsCameraFollowingMockedLocation(newValue)
            viewModel.setIsCameraCurrentlyFollowingMockedLocation(newValue)
        },
        setIsGoingToWaitAtRouteFinish = { newValue ->
            viewModel.updateSettingsState { it.copy(isGoingToWaitAtRouteFinish = newValue) }
            viewModel.setIsGoingToWaitAtRouteFinish(newValue)
        },
        setIsShowingMapStyleDialog = { newValue ->
            viewModel.updateSettingsState { it.copy(isShowingMapStyleDialog = newValue) }
        },
        setIsShowingLocationAccuracyLevelDialog = { newValue ->
            viewModel.updateSettingsState { it.copy(isShowingLocationAccuracyLevelDialog = newValue) }
        },
        setIsShowingLocationUpdateDelayDialog = { newValue ->
            viewModel.updateSettingsState { it.copy(isShowingLocationUpdateDelayDialog = newValue) }
        },
        onConfigureExpandedControlsClicked = {
            navController.navigate(Screen.ExpandedControlsConfiguration.route)
        },
        onExportSettingsClicked = {
            navController.navigate(Screen.ExportSettings.route)
        },
        onImportSettingsClicked = {
            importLauncher.launch(arrayOf("application/json"))
        },
        setIsShowingResetSettingsDialog = { newValue ->
            viewModel.updateSettingsState { it.copy(isShowingResetSettingsDialog = newValue) }
        },
        onMapStyleSelected = { newValue ->
            viewModel.updateSettingsState { it.copy(mapStyle = newValue) }
            viewModel.setMapStyle(newValue)
        },
        onLocationAccuracyLevelSelected = { newValue ->
            viewModel.updateSettingsState { it.copy(locationAccuracyLevel = newValue) }
            viewModel.setLocationAccuracyLevel(newValue)
        },
        onLocationUpdateDelaySelected = { newValue ->
            viewModel.updateSettingsState { it.copy(locationUpdateDelay = newValue) }
            viewModel.setLocationUpdateDelay(newValue)
        },
        onResetSettingsToDefault = {
            viewModel.resetSettingsToDefault()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    state: SettingsState,
    onBack: () -> Unit = { },
    setBuildRouteOnRoad: (Boolean) -> Unit = { },
    setIsUsingCrosshairs: (Boolean) -> Unit = { },
    setClearRouteOnStop: (Boolean) -> Unit = { },
    setIsCameraFollowingMockedLocation: (Boolean) -> Unit = { },
    setIsGoingToWaitAtRouteFinish: (Boolean) -> Unit = { },
    setIsShowingMapStyleDialog: (Boolean) -> Unit = { },
    setIsShowingLocationAccuracyLevelDialog: (Boolean) -> Unit = { },
    setIsShowingLocationUpdateDelayDialog: (Boolean) -> Unit = { },
    onConfigureExpandedControlsClicked: () -> Unit = { },
    onExportSettingsClicked: () -> Unit = { },
    onImportSettingsClicked: () -> Unit = { },
    setIsShowingResetSettingsDialog: (Boolean) -> Unit = { },
    onMapStyleSelected: (MapStyle?) -> Unit = { },
    onLocationAccuracyLevelSelected: (LocationAccuracyLevel) -> Unit = { },
    onLocationUpdateDelaySelected: (Float) -> Unit = { },
    onResetSettingsToDefault: () -> Unit = { }
) {
    val context = LocalContext.current
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
                title = { Text("Settings") },
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
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            SwitchRow(
                label = "Build route on roads",
                checked = state.isBuildRouteOnRoads,
                onCheckedChange = {
                    setBuildRouteOnRoad(it)
                },
                switchTestTag = "build_route_on_roads_switch"
            )
            SwitchRow(
                label = "Use crosshairs",
                checked = state.isUsingCrosshairs,
                onCheckedChange = {
                    setIsUsingCrosshairs(it)
                },
                switchTestTag = "use_crosshairs_switch"
            )
            SwitchRow(
                label = "Clear route on stop",
                checked = state.clearPointsOnStop,
                onCheckedChange = {
                    setClearRouteOnStop(it)
                },
                switchTestTag = "clear_route_on_stop_switch"
            )
            SwitchRow(
                label = "Camera follows mocked location",
                checked = state.isCameraFollowingMockedLocation,
                onCheckedChange = {
                    setIsCameraFollowingMockedLocation(it)
                },
                switchTestTag = "camera_follows_mocked_location_switch"
            )
            SwitchRow(
                label = "Wait at the end of a route",
                checked = state.isGoingToWaitAtRouteFinish,
                onCheckedChange = {
                    setIsGoingToWaitAtRouteFinish(it)
                },
                switchTestTag = "wait_at_the_end_of_a_route_switch"
            )
            TextRow(
                label = "Map style",
                onClick = {
                    setIsShowingMapStyleDialog(true)
                },
                value = state.mapStyle?.name ?: "Default"
            )
            TextRow(
                label = "Location accuracy level",
                onClick = {
                    setIsShowingLocationAccuracyLevelDialog(true)
                },
                value = state.locationAccuracyLevel.name
            )
            TextRow(
                label = "Location update delay",
                onClick = {
                    setIsShowingLocationUpdateDelayDialog(true)
                },
                value = "${state.locationUpdateDelay.toTrimmedString()} s"
            )
            TextRow(
                label = "Configure expanded controls",
                onClick = {
                    onConfigureExpandedControlsClicked()
                }
            )
            TextRow(
                label = "Export settings",
                onClick = {
                    onExportSettingsClicked()
                }
            )
            TextRow(
                label = "Import settings",
                onClick = {
                    onImportSettingsClicked()
                }
            )
            TextRow(
                label = "Reset to default",
                onClick = {
                    setIsShowingResetSettingsDialog(true)
                }
            )
            TextRow(
                label = "Manual",
                onClick = {
                    Intent().apply {
                        action = Intent.ACTION_VIEW
                        data = "https://github.com/drew654/Mock-Locations/blob/master/README.md".toUri()
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }.also {
                        context.startActivity(it)
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
                    }.also {
                        context.startActivity(it)
                    }
                }
            )
        }
    }

    MapStyleDialog(
        isVisible = state.isShowingMapStyleDialog,
        onDismiss = { setIsShowingMapStyleDialog(false) },
        selectedStyle = state.mapStyle,
        onStyleSelected = {
            onMapStyleSelected(it)
        }
    )

    LocationAccuracyLevelDialog(
        isVisible = state.isShowingLocationAccuracyLevelDialog,
        selectedLevel = state.locationAccuracyLevel,
        onDismiss = { setIsShowingLocationAccuracyLevelDialog(false) },
        onLevelSelected = {
            onLocationAccuracyLevelSelected(it)
        }
    )

    LocationUpdateDelayDialog(
        isVisible = state.isShowingLocationUpdateDelayDialog,
        onDismiss = { setIsShowingLocationUpdateDelayDialog(false) },
        locationUpdateDelay = state.locationUpdateDelay,
        onLocationUpdateDelayChanged = {
            onLocationUpdateDelaySelected(it)
        }
    )

    ResetSettingsDialog(
        isVisible = state.isShowingResetSettingsDialog,
        onConfirm = {
            onResetSettingsToDefault()
            setIsShowingResetSettingsDialog(false)
        },
        onDismiss = {
            setIsShowingResetSettingsDialog(false)
        }
    )
}

@DayNightDevicePreviews
@Composable
private fun SettingsScreenPreview() {
    DeviceThemePreview {
        SettingsContent(
            state = SettingsState()
        )
    }
}
