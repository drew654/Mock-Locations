package com.drew654.mocklocations.presentation.settings_screen

import android.content.Intent
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.LocationAccuracyLevel
import com.drew654.mocklocations.domain.model.MapStyle
import com.drew654.mocklocations.domain.model.SettingsState
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
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavController
) {
    val state by viewModel.state.collectAsState()

    SettingsContent(
        state = state,
        onBack = {
            navController.popBackStack()
        },
        setBuildRouteOnRoad = { newValue ->
            viewModel.setIsBuildRouteOnRoads(newValue)
        },
        setIsUsingCrosshairs = { newValue ->
            viewModel.setIsUsingCrosshairs(newValue)
        },
        setClearRouteOnStop = { newValue ->
            viewModel.setClearRouteOnStop(newValue)
        },
        setIsCameraFollowingMockedLocation = { newValue ->
            viewModel.setIsCameraFollowingMockedLocation(newValue)
        },
        setIsGoingToWaitAtRouteFinish = { newValue ->
            viewModel.setIsGoingToWaitAtRouteFinish(newValue)
        },
        setIsShowingMapStyleDialog = { newValue ->
            viewModel.setIsShowingMapStyleDialog(newValue)
        },
        setIsShowingLocationAccuracyLevelDialog = { newValue ->
            viewModel.setIsShowingLocationAccuracyDialog(newValue)
        },
        setIsShowingLocationUpdateDelayDialog = { newValue ->
            viewModel.setIsShowingLocationUpdateDelayDialog(newValue)
        },
        onConfigureExpandedControlsClicked = {
            navController.navigate(Screen.ExpandedControlsConfiguration.route)
        },
        onExportSettingsClicked = {
            navController.navigate(Screen.ExportSettings.route)
        },
        onImportSettingsClicked = {
            navController.navigate(Screen.ImportSettings.route)
        },
        setIsShowingResetSettingsDialog = { newValue ->
            viewModel.setIsShowingResetSettingsDialog(newValue)
        },
        onMapStyleSelected = { newValue ->
            viewModel.setMapStyle(newValue)
        },
        onLocationAccuracyLevelSelected = { newValue ->
            viewModel.setLocationAccuracyLevel(newValue)
        },
        onLocationUpdateDelaySelected = { newValue ->
            viewModel.setLocationUpdateDelay(newValue)
        },
        onResetSettingsToDefault = {
            viewModel.resetSettingsToDefault()
        },
        onManageRoutesClicked = {
            navController.navigate(Screen.ManageRoutes.route)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsContent(
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
    onResetSettingsToDefault: () -> Unit = { },
    onManageRoutesClicked: () -> Unit = { }
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
                label = "Manage routes",
                onClick = {
                    onManageRoutesClicked()
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
