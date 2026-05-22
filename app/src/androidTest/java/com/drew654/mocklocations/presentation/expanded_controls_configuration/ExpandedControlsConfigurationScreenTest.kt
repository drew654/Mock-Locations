package com.drew654.mocklocations.presentation.expanded_controls_configuration

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.drew654.mocklocations.domain.model.ExpandedControlsConfigurationState
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExpandedControlsConfigurationScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

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
}
