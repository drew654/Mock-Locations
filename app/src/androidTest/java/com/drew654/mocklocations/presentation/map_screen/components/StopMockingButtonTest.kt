package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class StopMockingButtonTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickButton_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            StopMockingButton(
                onClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Stop").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickButton_disabled_whenStateDisabled() {
        var clicked = false
        composeTestRule.setContent {
            StopMockingButton(
                onClick = { clicked = true },
                enabled = false
            )
        }

        composeTestRule.onNodeWithContentDescription("Stop").performClick()

        assertFalse(clicked)
    }
}
