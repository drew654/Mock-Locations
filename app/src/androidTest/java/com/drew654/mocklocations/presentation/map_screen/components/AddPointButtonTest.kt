package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AddPointButtonTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickButton_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            AddPointButton(
                onClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Add Point").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickButton_disabled_whenDisabledState() {
        var clicked = false
        composeTestRule.setContent {
            AddPointButton(
                onClick = { clicked = true },
                enabled = false
            )
        }

        composeTestRule.onNodeWithContentDescription("Add Point").performClick()
        assertFalse(clicked)
    }
}
