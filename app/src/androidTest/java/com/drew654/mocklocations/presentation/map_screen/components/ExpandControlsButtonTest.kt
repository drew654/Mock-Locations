package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExpandControlsButtonTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickButton_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ExpandControlsButton(
                controlsAreExpanded = false,
                onClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Expand controls").performClick()

        assertTrue(clicked)
    }

    @Test
    fun buttonHasCorrectContentDescription() {
        val expanded = mutableStateOf(false)
        composeTestRule.setContent {
            ExpandControlsButton(
                controlsAreExpanded = expanded.value,
                onClick = { expanded.value = !expanded.value }
            )
        }

        composeTestRule.onNodeWithContentDescription("Expand controls").assertExists()

        composeTestRule.onNodeWithContentDescription("Expand controls").performClick()

        composeTestRule.onNodeWithContentDescription("Collapse controls").assertExists()
    }
}
