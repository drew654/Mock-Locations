package com.drew654.mocklocations.presentation.expanded_controls_configuration

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.NavController
import com.drew654.mocklocations.domain.model.ExpandedControlsConfigurationState
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExpandedControlsConfigurationScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<MockLocationsViewModel>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val expandedControlsConfigurationState = MutableStateFlow(ExpandedControlsConfigurationState())

    private fun setupMockFlows() {
        expandedControlsConfigurationState.value = ExpandedControlsConfigurationState()
        every { viewModel.expandedControlsConfigurationState } returns expandedControlsConfigurationState

        every { viewModel.updateExpandedControlsConfigurationState(any()) } answers {
            val transform = firstArg<(ExpandedControlsConfigurationState) -> ExpandedControlsConfigurationState>()
            expandedControlsConfigurationState.value = transform(expandedControlsConfigurationState.value)
        }
    }

    @Test
    fun backButton_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ExpandedControlsConfigurationContent(
                state = ExpandedControlsConfigurationState(),
                onBack = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickSpeedUnit_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ExpandedControlsConfigurationContent(
                state = ExpandedControlsConfigurationState(),
                setIsShowingDialog = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Speed unit").performClick()

        assertTrue(clicked)
    }

    @Test
    fun setSpeedUnitValue_triggersCallback() {
        var input: SpeedUnitValue? = null
        composeTestRule.setContent {
            ExpandedControlsConfigurationContent(
                state = ExpandedControlsConfigurationState(
                    speedUnitValue = SpeedUnitValue(30.0, SpeedUnit.MilesPerHour),
                    isShowingDialog = true
                ),
                setSpeedUnitValue = { input = it }
            )
        }

        composeTestRule.onNodeWithText("m/s").performClick()

        assertTrue(input == SpeedUnitValue(30.0, SpeedUnit.MetersPerSecond))
    }

    @Test
    fun setSpeedSliderLowerEnd_triggersCallback() {
        var input: String? = null
        composeTestRule.setContent {
            ExpandedControlsConfigurationContent(
                state = ExpandedControlsConfigurationState(),
                setSpeedSliderLowerEnd = { input = it }
            )
        }

        composeTestRule.onNodeWithText("0").performTextReplacement("50")

        assert(input == "50")
    }

    @Test
    fun setSpeedSliderUpperEnd_triggersCallback() {
        var input: String? = null
        composeTestRule.setContent {
            ExpandedControlsConfigurationContent(
                state = ExpandedControlsConfigurationState(),
                setSpeedSliderUpperEnd = { input = it }
            )
        }

        composeTestRule.onNodeWithText("100").performTextReplacement("200")

        assert(input == "200")
    }

    @Test
    fun saveButton_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ExpandedControlsConfigurationContent(
                state = ExpandedControlsConfigurationState(),
                onSave = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Save").performClick()

        assertTrue(clicked)
    }

    @Test
    fun integration_onOpen_refreshExpandedControlsConfigurationState() {
        setupMockFlows()
        composeTestRule.setContent {
            ExpandedControlsConfigurationScreen(viewModel = viewModel, navController = navController)
        }

        verify { viewModel.refreshExpandedControlsConfigurationState() }
    }

    @Test
    fun integration_backButton_callsViewModelAndNavController() {
        setupMockFlows()
        composeTestRule.setContent {
            ExpandedControlsConfigurationScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        verify { navController.popBackStack() }
    }

    @Test
    fun integration_selectSpeedUnit_updatesUi() {
        setupMockFlows()
        composeTestRule.setContent {
            ExpandedControlsConfigurationScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Speed unit").performClick()

        composeTestRule.onNodeWithText("m/s").performClick()

        assert(expandedControlsConfigurationState.value.speedUnitValue == SpeedUnitValue(30.0, SpeedUnit.MetersPerSecond))
        composeTestRule.onNodeWithText("m/s").assertExists()
    }

    @Test
    fun integration_setSpeedSliderLowerEnd_updatesViewModelAndUi() {
        setupMockFlows()
        composeTestRule.setContent {
            ExpandedControlsConfigurationScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("0").performTextReplacement("50")

        assert(expandedControlsConfigurationState.value.speedSliderLowerEnd == "50")
        composeTestRule.onNodeWithText("50").assertExists()
    }

    @Test
    fun integration_setSpeedSliderUpperEnd_updatesViewModelAndUi() {
        setupMockFlows()
        composeTestRule.setContent {
            ExpandedControlsConfigurationScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("100").performTextReplacement("200")

        assert(expandedControlsConfigurationState.value.speedSliderUpperEnd == "200")
        composeTestRule.onNodeWithText("200").assertExists()
    }

    @Test
    fun integration_clickSave_callsViewModelAndNavController() {
        setupMockFlows()
        composeTestRule.setContent {
            ExpandedControlsConfigurationScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Save").performClick()

        verify { viewModel.saveExpandedControlsConfigurationState() }
        verify { navController.popBackStack() }
    }
}
