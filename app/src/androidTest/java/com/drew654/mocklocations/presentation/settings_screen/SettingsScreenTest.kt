package com.drew654.mocklocations.presentation.settings_screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.NavController
import com.drew654.mocklocations.domain.model.LocationAccuracyLevel
import com.drew654.mocklocations.domain.model.MapStyle
import com.drew654.mocklocations.domain.model.SettingsState
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import com.drew654.mocklocations.presentation.Screen
import com.drew654.mocklocations.presentation.toTrimmedString
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<MockLocationsViewModel>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val settingsState = MutableStateFlow(SettingsState())

    private fun setupMockFlows() {
        settingsState.value = SettingsState()
        every { viewModel.settingsState } returns settingsState

        every { viewModel.updateSettingsState(any()) } answers {
            val transform = firstArg<(SettingsState) -> SettingsState>()
            settingsState.value = transform(settingsState.value)
        }
    }

    @Test
    fun backButton_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                state = SettingsState(),
                onBack = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(clicked)
    }

    @Test
    fun toggleBuildRouteOnRoads_triggersCallback() {
        var capturedValue: Boolean? = null
        composeTestRule.setContent {
            SettingsContent(
                state = SettingsState(isBuildRouteOnRoads = false),
                setBuildRouteOnRoad = { capturedValue = it }
            )
        }

        composeTestRule.onNodeWithText("Build route on roads").performClick()

        assertEquals(true, capturedValue)
    }

    @Test
    fun toggleUseCrosshairs_triggersCallback() {
        var capturedValue: Boolean? = null
        composeTestRule.setContent {
            SettingsContent(
                state = SettingsState(isUsingCrosshairs = false),
                setIsUsingCrosshairs = { capturedValue = it }
            )
        }

        composeTestRule.onNodeWithText("Use crosshairs").performClick()

        assertEquals(true, capturedValue)
    }

    @Test
    fun toggleClearRouteOnStop_triggersCallback() {
        var capturedValue: Boolean? = null
        composeTestRule.setContent {
            SettingsContent(
                state = SettingsState(clearPointsOnStop = false),
                setClearRouteOnStop = { capturedValue = it }
            )
        }

        composeTestRule.onNodeWithText("Clear route on stop").performClick()

        assertEquals(true, capturedValue)
    }

    @Test
    fun toggleCameraFollowsMockedLocation_triggersCallback() {
        var capturedValue: Boolean? = null
        composeTestRule.setContent {
            SettingsContent(
                state = SettingsState(isCameraFollowingMockedLocation = false),
                setIsCameraFollowingMockedLocation = { capturedValue = it }
            )
        }

        composeTestRule.onNodeWithText("Camera follows mocked location").performClick()

        assertEquals(true, capturedValue)
    }

    @Test
    fun toggleWaitAtTheEndOfARoute_triggersCallback() {
        var capturedValue: Boolean? = null
        composeTestRule.setContent {
            SettingsContent(
                state = SettingsState(isGoingToWaitAtRouteFinish = false),
                setIsGoingToWaitAtRouteFinish = { capturedValue = it }
            )
        }

        composeTestRule.onNodeWithText("Wait at the end of a route").performClick()

        assertEquals(true, capturedValue)
    }

    @Test
    fun clickMapStyle_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                state = SettingsState(),
                setIsShowingMapStyleDialog = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Map style").performScrollTo().performClick()

        assertTrue(clicked)
    }

    @Test
    fun mapStyleDialog_isShown_whenStateIsTrue() {
        composeTestRule.setContent {
            SettingsContent(state = SettingsState(isShowingMapStyleDialog = true))
        }

        listOf("Default (System)", "Standard", "Night", "Satellite", "Hybrid", "Terrain", "Aubergine", "Dark", "Retro", "Silver").forEach {
            composeTestRule.onNodeWithText(it).performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun onMapStyleSelected_triggersCallback() {
        var selectedStyle: MapStyle? = null
        composeTestRule.setContent {
            SettingsContent(
                state = SettingsState(isShowingMapStyleDialog = true),
                onMapStyleSelected = { selectedStyle = it }
            )
        }

        composeTestRule.onNodeWithText("Satellite").performClick()

        assertEquals(MapStyle.Satellite, selectedStyle)
    }

    @Test
    fun clickLocationAccuracyLevel_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                state = SettingsState(),
                setIsShowingLocationAccuracyLevelDialog = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Location accuracy level").performScrollTo().performClick()

        assertTrue(clicked)
    }

    @Test
    fun locationAccuracyLevelDialog_isShown_whenStateIsTrue() {
        composeTestRule.setContent {
            SettingsContent(state = SettingsState(isShowingLocationAccuracyLevelDialog = true))
        }

        listOf("Perfect (0 m)", "High (5 m)", "Medium (10 m)", "Low (20 m)").forEach {
            composeTestRule.onNodeWithText(it).assertIsDisplayed()
        }
    }

    @Test
    fun onLocationAccuracyLevelSelected_triggersCallback() {
        var selectedLevel: LocationAccuracyLevel? = null
        composeTestRule.setContent {
            SettingsContent(
                state = SettingsState(isShowingLocationAccuracyLevelDialog = true),
                onLocationAccuracyLevelSelected = { selectedLevel = it }
            )
        }

        composeTestRule.onNodeWithText("High (5 m)").performClick()

        assertEquals(LocationAccuracyLevel.High, selectedLevel)
    }

    @Test
    fun clickLocationUpdateDelay_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                state = SettingsState(),
                setIsShowingLocationUpdateDelayDialog = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Location update delay").performScrollTo().performClick()

        assertTrue(clicked)
    }

    @Test
    fun locationUpdateDelayDialog_isShown_whenStateIsTrue() {
        composeTestRule.setContent {
            SettingsContent(state = SettingsState(isShowingLocationUpdateDelayDialog = true))
        }

        composeTestRule.onNodeWithText("Location Update Delay").assertIsDisplayed()

        composeTestRule.onNodeWithText("Delay between location updates in seconds").assertIsDisplayed()
    }

    @Test
    fun onLocationUpdateDelaySelected_triggersCallback() {
        var selectedDelay: Float? = null
        composeTestRule.setContent {
            SettingsContent(
                state = SettingsState(
                    isShowingLocationUpdateDelayDialog = true,
                    locationUpdateDelay = 1f
                ),
                onLocationUpdateDelaySelected = { selectedDelay = it }
            )
        }

        composeTestRule.onNodeWithText("1").performTextReplacement("2.5")
        composeTestRule.onNodeWithText("Save").performClick()

        assertEquals(2.5f, selectedDelay)
    }

    @Test
    fun locationUpdateDelayDialog_validation_enablesAndDisablesSaveButton() {
        composeTestRule.setContent {
            SettingsContent(
                state = SettingsState(
                    isShowingLocationUpdateDelayDialog = true,
                    locationUpdateDelay = 1f
                )
            )
        }

        listOf("0", "1000", "", "e", "1e", "1 .", "1 ,", "6 7", "1.2.3", "1,2,3", "1,2.3", "1.2,3").forEach {
            composeTestRule.onNode(hasSetTextAction()).performTextReplacement(it)
            composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
            composeTestRule.onNodeWithText("Cancel").assertIsEnabled()
        }

        listOf(" 1", "1 ", " 1 ", "1.", " 1.", "1.2", "1,2", " 1.2 ", " 1,2 ", "1.23", "1.234", "1.235").forEach {
            composeTestRule.onNode(hasSetTextAction()).performTextReplacement(it)
            composeTestRule.onNodeWithText("Save").assertIsEnabled()
            composeTestRule.onNodeWithText("Cancel").assertIsEnabled()
        }
    }

    @Test
    fun clickConfigureExpandedControls_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                state = SettingsState(),
                onConfigureExpandedControlsClicked = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Configure expanded controls").performScrollTo().performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickExportSettings_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                state = SettingsState(),
                onExportSettingsClicked = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Export settings").performScrollTo().performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickImportSettings_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                state = SettingsState(),
                onImportSettingsClicked = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Import settings").performScrollTo().performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickResetToDefault_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                state = SettingsState(),
                setIsShowingResetSettingsDialog = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Reset to default").performScrollTo().performClick()

        assertTrue(clicked)
    }

    @Test
    fun resetSettingsDialog_isShown_whenStateIsTrue() {
        composeTestRule.setContent {
            SettingsContent(state = SettingsState(isShowingResetSettingsDialog = true))
        }

        composeTestRule.onNodeWithText("Reset settings to default?").assertIsDisplayed()
    }

    @Test
    fun onResetSettingsToDefault_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                state = SettingsState(isShowingResetSettingsDialog = true),
                onResetSettingsToDefault = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Reset Settings").performClick()

        assertTrue(clicked)
    }

    @Test
    fun integration_onOpen_refreshSettingsState() {
        setupMockFlows()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }

        verify { viewModel.refreshSettingsState() }
    }

    @Test
    fun integration_backButton_callsViewModelAndNavController() {
        setupMockFlows()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        verify { viewModel.setShouldFocusSearchBar(false) }
        verify { navController.popBackStack() }
    }

    @Test
    fun integration_toggleBuildRouteOnRoads_callsViewModel() {
        setupMockFlows()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Build route on roads").performClick()

        verify { viewModel.setBuildRouteOnRoads(true) }
        assertTrue(settingsState.value.isBuildRouteOnRoads)
    }

    @Test
    fun integration_toggleUseCrosshairs_callsViewModel() {
        setupMockFlows()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Use crosshairs").performClick()

        verify { viewModel.setIsUsingCrosshairs(false) }
        assertFalse(settingsState.value.isUsingCrosshairs)
    }

    @Test
    fun integration_toggleClearRouteOnStop_callsViewModel() {
        setupMockFlows()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Clear route on stop").performClick()

        verify { viewModel.setClearRouteOnStop(true) }
        assertTrue(settingsState.value.clearPointsOnStop)
    }

    @Test
    fun integration_toggleCameraFollowsMockedLocation_callsViewModel() {
        setupMockFlows()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Camera follows mocked location").performClick()

        verify { viewModel.setIsCameraFollowingMockedLocation(false) }
        assertFalse(settingsState.value.isCameraFollowingMockedLocation)
    }

    @Test
    fun integration_toggleWaitAtTheEndOfARoute() {
        setupMockFlows()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Wait at the end of a route").performClick()

        verify { viewModel.setIsGoingToWaitAtRouteFinish(true) }
        assertTrue(settingsState.value.isGoingToWaitAtRouteFinish)
    }

    @Test
    fun integration_selectMapStyle_updatesViewModelAndUi() {
        setupMockFlows()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Map style").performScrollTo().performClick()

        composeTestRule.onNodeWithText("Satellite").performClick()

        verify { viewModel.setMapStyle(MapStyle.Satellite) }
        assertEquals(MapStyle.Satellite, settingsState.value.mapStyle)
        composeTestRule.onNodeWithText("Satellite").assertIsDisplayed()
    }

    @Test
    fun integration_selectLocationAccuracyLevel_updatesViewModelAndUi() {
        setupMockFlows()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Location accuracy level").performScrollTo().performClick()

        composeTestRule.onNodeWithText("High (5 m)").performClick()

        verify { viewModel.setLocationAccuracyLevel(LocationAccuracyLevel.High) }
        assertEquals(LocationAccuracyLevel.High, settingsState.value.locationAccuracyLevel)
        composeTestRule.onNodeWithText("High").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun integration_selectLocationUpdateDelay_callsViewModel() {
        setupMockFlows()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Location update delay").performScrollTo().performClick()

        composeTestRule.onNodeWithText("1").performTextReplacement("3.5")
        composeTestRule.onNodeWithText("Save").performClick()

        verify { viewModel.setLocationUpdateDelay(3.5f) }
        assertEquals(3.5f, settingsState.value.locationUpdateDelay)
    }

    @Test
    fun integration_selectLocationUpdateDelay_updatesViewModelAndUi() {
        setupMockFlows()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Location update delay").performScrollTo().performClick()

        listOf("0", "1000", "", "e", "1e", "1 .", "1 ,", "6 7", "1.2.3", "1,2,3", "1,2.3", "1.2,3").forEach {
            composeTestRule.onNode(hasSetTextAction()).performTextReplacement(it)
            composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
            composeTestRule.onNodeWithText("Cancel").assertIsEnabled()
        }

        composeTestRule.onNodeWithText("Cancel").performClick()

        listOf(
            Pair(" 1", 1f),
            Pair("1 ", 1f),
            Pair(" 1 ", 1f),
            Pair("1.", 1f),
            Pair(" 1.", 1f),
            Pair("1.2", 1.2f),
            Pair("1,2", 1.2f),
            Pair(" 1.2 ", 1.2f),
            Pair(" 1,2 ", 1.2f),
            Pair("1.23", 1.23f),
            Pair("1.234", 1.23f),
            Pair("1.235", 1.24f)
        ).forEach { (input, expectedFloat) ->
            val expectedText = "${expectedFloat.toTrimmedString()} s"

            composeTestRule.onNodeWithText("Location update delay").performClick()

            composeTestRule.onNode(hasSetTextAction()).performTextReplacement(input)

            composeTestRule.onNodeWithText("Cancel").assertIsEnabled()
            composeTestRule.onNodeWithText("Save").assertIsEnabled()
            composeTestRule.onNodeWithText("Save").performClick()

            verify { viewModel.setLocationUpdateDelay(expectedFloat) }

            composeTestRule.onNodeWithText(expectedText).assertIsDisplayed()
        }
    }

    @Test
    fun integration_clickConfigureExpandedControls_navigatesToExpandedControlsScreen() {
        setupMockFlows()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Configure expanded controls").performScrollTo().performClick()

        verify { navController.navigate(Screen.ExpandedControlsConfiguration.route) }
    }

    @Test
    fun integration_clickExportSettings_navigatesToExportScreen() {
        setupMockFlows()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Export settings").performScrollTo().performClick()

        verify { navController.navigate(Screen.ExportSettings.route) }
    }

    @Test
    fun integration_resetToDefault_callsViewModel() {
        setupMockFlows()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Reset to default").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Reset Settings").performClick()

        verify { viewModel.resetSettingsToDefault() }
        assert(viewModel.settingsState.value == SettingsState())
    }
}
