package com.drew654.mocklocations.presentation.settings_screen

import android.content.Intent
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

        val locationAccuracyLevel = MutableStateFlow<LocationAccuracyLevel>(LocationAccuracyLevel.Perfect)
        every { viewModel.locationAccuracyLevel } returns locationAccuracyLevel

        val locationUpdateDelay = MutableStateFlow(1f)
        every { viewModel.locationUpdateDelay } returns locationUpdateDelay

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
