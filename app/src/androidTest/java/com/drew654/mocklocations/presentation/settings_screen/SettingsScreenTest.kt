package com.drew654.mocklocations.presentation.settings_screen

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import com.drew654.mocklocations.domain.model.LocationAccuracyLevel
import com.drew654.mocklocations.domain.model.MapStyle
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
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
    fun toggleBuildRouteOnRoads_worksCorrectly() {
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
    fun toggleWaitAtRouteFinish_updatesUIState() {
        setupMockFlows()

        composeTestRule.setContent {
            SettingsScreen(viewModel, navController)
        }

        composeTestRule
            .onNodeWithTag("wait_at_route_finish_switch")
            .assertIsOff()

        composeTestRule
            .onNodeWithText("Wait at the end of a route")
            .performClick()

        verify { viewModel.setIsGoingToWaitAtRouteFinish(true) }

        composeTestRule
            .onNodeWithTag("wait_at_route_finish_switch")
            .assertIsOn()
    }
}
