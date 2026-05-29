package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NamingRouteDialogBodyTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickBack_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            NamingRouteDialogBody(
                routeName = TextFieldValue(""),
                isSaveEnabled = false,
                onBack = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Back").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickSaveRoute_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            NamingRouteDialogBody(
                routeName = TextFieldValue("Route 1"),
                isSaveEnabled = true,
                onConfirm = { clicked = true }
            )
        }

        composeTestRule.onNodeWithTag("save_route_name_button").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickSaveRoute_disabled_whenStateDisabled() {
        var clicked = false
        composeTestRule.setContent {
            NamingRouteDialogBody(
                routeName = TextFieldValue(""),
                isSaveEnabled = false,
                onConfirm = { clicked = true }
            )
        }

        composeTestRule.onNodeWithTag("save_route_name_button").assertIsNotEnabled()

        composeTestRule.onNodeWithTag("save_route_name_button").performClick()

        assertFalse(clicked)
    }
}
