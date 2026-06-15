package com.drew654.mocklocations.presentation.expanded_controls_configuration

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.NavController
import com.drew654.mocklocations.domain.model.ExpandedControlsConfigurationState
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExpandedControlsConfigurationScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<ExpandedControlsConfigurationViewModel>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val state = MutableStateFlow(ExpandedControlsConfigurationState())

    private fun setupMockFlows() {
        state.value = ExpandedControlsConfigurationState()
        every { viewModel.state } returns state
    }

    @Test
    fun clickBackButton_triggersCallback() {
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

        assertEquals(SpeedUnitValue(30.0, SpeedUnit.MetersPerSecond), input)
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

        assertEquals("50", input)
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

        assertEquals("200", input)
    }

    @Test
    fun clickSaveButton_triggersCallback() {
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
    fun clickSaveButton_doesNotTriggerCallback_whenFormIsInvalid() {
        var clicked = false
        composeTestRule.setContent {
            ExpandedControlsConfigurationContent(
                state = ExpandedControlsConfigurationState(
                    speedSliderLowerEnd = "100",
                    speedSliderUpperEnd = "50"
                ),
                onSave = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Save").performClick()

        assertFalse(clicked)
    }

    @Test
    fun dialog_isShown_whenStateIsTrue() {
        composeTestRule.setContent {
            ExpandedControlsConfigurationContent(
                state = ExpandedControlsConfigurationState(
                    isShowingDialog = true
                )
            )
        }

        listOf("km/h", "m/s", "mph").forEach { unit ->
            val expectedCount = if (unit == state.value.speedUnitValue.speedUnit.name) 2 else 1
            composeTestRule.onAllNodesWithText(unit).assertCountEquals(expectedCount)
        }
    }

    @Test
    fun integration_backButton_callsNavController() {
        setupMockFlows()

        composeTestRule.setContent {
            ExpandedControlsConfigurationScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        verify { navController.popBackStack() }
    }

    @Test
    fun integration_selectSpeedUnit_updatesViewModelAndUi() {
        setupMockFlows()

        every { viewModel.setIsShowingDialog(any()) } answers {
            state.value = state.value.copy(isShowingDialog = firstArg())
        }
        every { viewModel.setSpeedUnitValue(any()) } answers {
            state.value = state.value.copy(speedUnitValue = firstArg())
        }

        composeTestRule.setContent {
            ExpandedControlsConfigurationScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Speed unit").performClick()

        verify { viewModel.setIsShowingDialog(true) }

        composeTestRule.onNodeWithText("m/s").performClick()

        verify { viewModel.setSpeedUnitValue(SpeedUnitValue(30.0, SpeedUnit.MetersPerSecond)) }
        assertEquals(SpeedUnitValue(30.0, SpeedUnit.MetersPerSecond), state.value.speedUnitValue)
        composeTestRule.onNodeWithText("m/s").assertIsDisplayed()
    }

    @Test
    fun integration_setSpeedSliderLowerEnd_updatesViewModelAndUi() {
        setupMockFlows()

        every { viewModel.setSpeedSliderLowerEnd(any()) } answers {
            state.value = state.value.copy(speedSliderLowerEnd = firstArg())
        }

        composeTestRule.setContent {
            ExpandedControlsConfigurationScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("0").performTextReplacement("50")

        verify { viewModel.setSpeedSliderLowerEnd("50") }

        assertEquals("50", state.value.speedSliderLowerEnd)
        composeTestRule.onNodeWithText("50").assertIsDisplayed()
    }

    @Test
    fun integration_setSpeedSliderUpperEnd_updatesViewModelAndUi() {
        setupMockFlows()

        every { viewModel.setSpeedSliderUpperEnd(any()) } answers {
            state.value = state.value.copy(speedSliderUpperEnd = firstArg())
        }

        composeTestRule.setContent {
            ExpandedControlsConfigurationScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("100").performTextReplacement("200")

        verify { viewModel.setSpeedSliderUpperEnd("200") }

        assertEquals("200", state.value.speedSliderUpperEnd)
        composeTestRule.onNodeWithText("200").assertIsDisplayed()
    }

    @Test
    fun integration_clickSave_callsViewModelAndNavController() {
        setupMockFlows()

        every { viewModel.save(captureLambda()) } answers {
            lambda<() -> Unit>().invoke()
        }

        composeTestRule.setContent {
            ExpandedControlsConfigurationScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Save").performClick()

        verify { viewModel.save(any()) }
        verify { navController.popBackStack() }
    }
}
