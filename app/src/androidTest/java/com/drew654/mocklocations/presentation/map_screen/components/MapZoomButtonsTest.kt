package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MapZoomButtonsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickZoomIn_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapZoomButtons(
                onZoomIn = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Zoom in").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickZoomOut_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapZoomButtons(
                onZoomOut = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Zoom out").performClick()

        assertTrue(clicked)
    }
}
