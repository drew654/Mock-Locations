package com.drew654.mocklocations.presentation.settings_screen

import android.content.Intent
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.NavController
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasFlags
import com.drew654.mocklocations.domain.model.LocationAccuracyLevel
import com.drew654.mocklocations.domain.model.MapStyle
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import com.drew654.mocklocations.presentation.Screen
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<MockLocationsViewModel>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)

    private fun setupMockFlows() {
        val mockControlStateFlow = MutableStateFlow(MockControlState())
        every { viewModel.mockControlState } returns mockControlStateFlow
        every { viewModel.setIsUsingCrosshairs(any()) } answers {
            mockControlStateFlow.value = mockControlStateFlow.value.copy(isUsingCrosshairs = firstArg())
        }

        val isBuildRoutesOnRoadFlow = MutableStateFlow(false)
        every { viewModel.isBuildRoutesOnRoad } returns isBuildRoutesOnRoadFlow
        every { viewModel.setBuildRouteOnRoads(any()) } answers {
            isBuildRoutesOnRoadFlow.value = firstArg()
        }

        val clearPointsOnStop = MutableStateFlow(false)
        every { viewModel.clearRouteOnStop } returns clearPointsOnStop
        every { viewModel.setClearRouteOnStop(any()) } answers {
            clearPointsOnStop.value = firstArg()
        }

        val mapStyle = MutableStateFlow<MapStyle?>(null)
        every { viewModel.mapStyle } returns mapStyle
        every { viewModel.setMapStyle(any()) } answers {
            mapStyle.value = firstArg()
        }

        val locationAccuracyLevel = MutableStateFlow<LocationAccuracyLevel>(LocationAccuracyLevel.Perfect)
        every { viewModel.locationAccuracyLevel } returns locationAccuracyLevel
        every { viewModel.setLocationAccuracyLevel(any()) } answers {
            locationAccuracyLevel.value = firstArg()
        }

        val locationUpdateDelay = MutableStateFlow(1f)
        every { viewModel.locationUpdateDelay } returns locationUpdateDelay
        every { viewModel.setLocationUpdateDelay(any()) } answers {
            locationUpdateDelay.value = firstArg()
        }

        val isCameraFollowingMockedLocation = MutableStateFlow(true)
        every { viewModel.isCameraFollowingMockedLocation } returns isCameraFollowingMockedLocation
        every { viewModel.setIsCameraFollowingMockedLocation(any()) } answers {
            isCameraFollowingMockedLocation.value = firstArg()
        }

        val isGoingToWaitAtRouteFinish = MutableStateFlow(false)
        every { viewModel.isGoingToWaitAtRouteFinish } returns isGoingToWaitAtRouteFinish
        every { viewModel.setIsGoingToWaitAtRouteFinish(any()) } answers {
            isGoingToWaitAtRouteFinish.value = firstArg()
        }
    }

    @Test
    fun toggleBuildRouteOnRoads_updatesUIState() {
        setupMockFlows()

        composeTestRule.setContent {
            SettingsScreen(viewModel, navController)
        }

        composeTestRule
            .onNodeWithTag("build_route_on_roads_switch")
            .assertIsOff()

        composeTestRule
            .onNodeWithText("Build route on roads")
            .performClick()

        verify { viewModel.setBuildRouteOnRoads(true) }

        composeTestRule
            .onNodeWithTag("build_route_on_roads_switch")
            .assertIsOn()
    }

    @Test
    fun toggleUseCrosshairs_updatesUIState() {
        setupMockFlows()

        composeTestRule.setContent {
            SettingsScreen(viewModel, navController)
        }

        composeTestRule
            .onNodeWithTag("use_crosshairs_switch")
            .assertIsOn()

        composeTestRule
            .onNodeWithText("Use crosshairs")
            .performClick()

        verify { viewModel.setIsUsingCrosshairs(false) }

        composeTestRule
            .onNodeWithTag("use_crosshairs_switch")
            .assertIsOff()
    }

    @Test
    fun toggleClearRouteOnStop_updatesUIState() {
        setupMockFlows()

        composeTestRule.setContent {
            SettingsScreen(viewModel, navController)
        }

        composeTestRule
            .onNodeWithTag("clear_route_on_stop_switch")
            .assertIsOff()

        composeTestRule
            .onNodeWithText("Clear route on stop")
            .performClick()

        verify { viewModel.setClearRouteOnStop(true) }

        composeTestRule
            .onNodeWithTag("clear_route_on_stop_switch")
            .assertIsOn()
    }

    @Test
    fun toggleCameraFollowsMockedLocation_updatesUIState() {
        setupMockFlows()

        composeTestRule.setContent {
            SettingsScreen(viewModel, navController)
        }

        composeTestRule
            .onNodeWithTag("camera_follows_mocked_location_switch")
            .assertIsOn()

        composeTestRule
            .onNodeWithText("Camera follows mocked location")
            .performClick()

        verify { viewModel.setIsCameraFollowingMockedLocation(false) }

        composeTestRule
            .onNodeWithTag("camera_follows_mocked_location_switch")
            .assertIsOff()
    }

    @Test
    fun toggleWaitAtTheEndOfARoute_updatesUIState() {
        setupMockFlows()

        composeTestRule.setContent {
            SettingsScreen(viewModel, navController)
        }

        composeTestRule
            .onNodeWithTag("wait_at_the_end_of_a_route_switch")
            .assertIsOff()

        composeTestRule
            .onNodeWithText("Wait at the end of a route")
            .performClick()

        verify { viewModel.setIsGoingToWaitAtRouteFinish(true) }

        composeTestRule
            .onNodeWithTag("wait_at_the_end_of_a_route_switch")
            .assertIsOn()
    }

    @Test
    fun clickMapStyle_updatesUIState() {
        setupMockFlows()

        composeTestRule.setContent {
            SettingsScreen(viewModel, navController)
        }

        composeTestRule
            .onNodeWithText("Default")
            .assertExists()

        composeTestRule
            .onNodeWithText("Map style")
            .performClick()

        listOf("Default (System)", "Standard", "Night", "Satellite", "Hybrid", "Terrain", "Aubergine", "Dark", "Retro", "Silver").forEach {
            composeTestRule
                .onNodeWithText(it)
                .assertExists()
        }

        composeTestRule
            .onNodeWithText("Hybrid")
            .performClick()

        verify { viewModel.setMapStyle(MapStyle.Hybrid) }

        composeTestRule
            .onNodeWithText("Default")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText("Hybrid")
            .assertExists()
    }

    @Test
    fun clickLocationAccuracyLevel_updatesUIState() {
        setupMockFlows()

        composeTestRule.setContent {
            SettingsScreen(viewModel, navController)
        }

        composeTestRule
            .onNodeWithText("Perfect")
            .assertExists()

        composeTestRule
            .onNodeWithText("Location accuracy level")
            .performClick()

        listOf("Perfect (0 m)", "High (5 m)", "Medium (10 m)", "Low (20 m)").forEach {
            composeTestRule
                .onNodeWithText(it)
                .assertExists()
        }

        composeTestRule
            .onNodeWithText("High (5 m)")
            .performClick()

        verify { viewModel.setLocationAccuracyLevel(LocationAccuracyLevel.High) }

        composeTestRule
            .onNodeWithText("Perfect")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText("High")
            .assertExists()
    }

    @Test
    fun clickLocationUpdateDelay_updatesUIState() {
        setupMockFlows()

        composeTestRule.setContent {
            SettingsScreen(viewModel, navController)
        }

        composeTestRule
            .onNodeWithText("Location Update Delay")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText("Delay between location updates in seconds")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText("Location update delay")
            .performClick()

        composeTestRule
            .onNodeWithText("Location Update Delay")
            .assertExists()

        composeTestRule
            .onNodeWithText("Delay between location updates in seconds")
            .assertExists()

        composeTestRule
            .onNode(hasSetTextAction())
            .assertIsFocused()

        composeTestRule
            .onNode(hasSetTextAction())
            .performTextReplacement("")

        composeTestRule
            .onNodeWithText("Cancel")
            .assertIsEnabled()

        composeTestRule
            .onNodeWithText("Save")
            .assertIsNotEnabled()

        composeTestRule
            .onNode(hasSetTextAction())
            .performTextInput("2")

        composeTestRule
            .onNodeWithText("Save")
            .performClick()

        verify { viewModel.setLocationUpdateDelay(2f) }

        composeTestRule
            .onNodeWithText("2 s")
            .assertExists()
    }

    @Test
    fun locationUpdateDelayDialog_handlesDecimalsAndCommas() {
        setupMockFlows()

        composeTestRule.setContent {
            SettingsScreen(viewModel, navController)
        }

        composeTestRule
            .onNodeWithText("Location update delay")
            .performClick()

        listOf("e", "1e", "1 .", "1 ,", "6 7", "1.2.3", "1,2,3", "1,2.3", "1.2,3").forEach {
            composeTestRule
                .onNode(hasSetTextAction())
                .performTextReplacement(it)

            composeTestRule
                .onNodeWithText("Cancel")
                .assertIsEnabled()

            composeTestRule
                .onNodeWithText("Save")
                .assertIsNotEnabled()
        }

        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()

        listOf(
            Triple(" 1", 1f, "1 s"),
            Triple("1 ", 1f, "1 s"),
            Triple(" 1 ", 1f, "1 s"),
            Triple("1.", 1f, "1 s"),
            Triple(" 1.", 1f, "1 s"),
            Triple("1.2", 1.2f, "1.2 s"),
            Triple("1,2", 1.2f, "1.2 s"),
            Triple(" 1.2 ", 1.2f, "1.2 s"),
            Triple(" 1,2 ", 1.2f, "1.2 s"),
            Triple("1.23", 1.23f, "1.23 s"),
            Triple("1.234", 1.23f, "1.23 s"),
            Triple("1.235", 1.24f, "1.24 s")
        ).forEach { (input, expectedFloat, expectedText) ->
            composeTestRule
                .onNodeWithText("Location update delay")
                .performClick()

            composeTestRule
                .onNode(hasSetTextAction())
                .performTextReplacement(input)

            composeTestRule
                .onNodeWithText("Cancel")
                .assertIsEnabled()

            composeTestRule
                .onNodeWithText("Save")
                .assertIsEnabled()

            composeTestRule
                .onNodeWithText("Save")
                .performClick()

            verify { viewModel.setLocationUpdateDelay(expectedFloat) }

            composeTestRule
                .onNodeWithText(expectedText)
                .assertExists()
        }
    }

    @Test
    fun locationUpdateDelayDialogButtons_dismissCorrectly() {
        setupMockFlows()

        composeTestRule.setContent {
            SettingsScreen(viewModel, navController)
        }

        composeTestRule
            .onNodeWithText("Location Update Delay")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText("Delay between location updates in seconds")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText("Location update delay")
            .performClick()

        composeTestRule
            .onNodeWithText("Location Update Delay")
            .assertExists()

        composeTestRule
            .onNodeWithText("Delay between location updates in seconds")
            .assertExists()

        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()

        composeTestRule
            .onNodeWithText("Location Update Delay")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText("Delay between location updates in seconds")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText("Location update delay")
            .performClick()

        composeTestRule
            .onNodeWithText("Location Update Delay")
            .assertExists()

        composeTestRule
            .onNodeWithText("Delay between location updates in seconds")
            .assertExists()

        composeTestRule
            .onNodeWithText("Save")
            .performClick()

        composeTestRule
            .onNodeWithText("Location Update Delay")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText("Delay between location updates in seconds")
            .assertDoesNotExist()
    }

    @Test
    fun clickConfigureExpandedControls_navigatesToCorrectScreen() {
        setupMockFlows()

        composeTestRule.setContent {
            SettingsScreen(viewModel, navController)
        }

        composeTestRule
            .onNodeWithText("Configure expanded controls")
            .performClick()

        verify { navController.navigate(Screen.ExpandedControlsConfiguration.route) }
    }

    @Test
    fun clickExportSettings_navigatesToCorrectScreen() {
        setupMockFlows()

        composeTestRule.setContent {
            SettingsScreen(viewModel, navController)
        }

        composeTestRule
            .onNodeWithText("Export settings")
            .performClick()

        verify { navController.navigate(Screen.ExportSettings.route) }
    }

    @Test
    fun clickManual_opensCorrectUrl() {
        setupMockFlows()
        Intents.init()

        try {
            composeTestRule.setContent {
                SettingsScreen(viewModel, navController)
            }

            composeTestRule
                .onNodeWithText("Manual")
                .performClick()

            val expectedUrl = "https://github.com/drew654/Mock-Locations/blob/master/README.md"

            intended(
                allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasData(expectedUrl)
                )
            )
        } finally {
            Intents.release()
        }
    }

    @Test
    fun clickPrivacyPolicy_opensCorrectUrl() {
        setupMockFlows()
        Intents.init()

        try {
            composeTestRule.setContent {
                SettingsScreen(viewModel, navController)
            }

            composeTestRule
                .onNodeWithText("Privacy policy")
                .performClick()

            val expectedUrl = "https://github.com/drew654/Mock-Locations/blob/master/PRIVACY_POLICY.md"

            intended(
                allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasData(expectedUrl),
                    hasFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            )
        } finally {
            Intents.release()
        }
    }
}
