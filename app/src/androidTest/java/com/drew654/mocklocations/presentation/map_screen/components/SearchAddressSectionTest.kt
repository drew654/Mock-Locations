package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SearchAddressSectionTest {
    @get:Rule
    val composeTestRule = createComposeRule()


    @Test
    fun typingText_showsClearButton() {
        composeTestRule.setContent {
            SearchAddressSection(shouldFocusSearchBar = false)
        }

        composeTestRule.onNodeWithContentDescription("Clear").assertDoesNotExist()

        composeTestRule.onNodeWithText("Search address").performTextInput("400 Bizzell St.")

        composeTestRule.onNodeWithContentDescription("Clear").assertIsDisplayed()
    }

    @Test
    fun clickClearButton_clearsText() {
        composeTestRule.setContent {
            SearchAddressSection(shouldFocusSearchBar = false)
        }

        composeTestRule.onNodeWithText("Search address").performTextInput("400 Bizzell St.")

        composeTestRule.onNodeWithContentDescription("Clear").performClick()

        composeTestRule.onNodeWithText("Search address").assertTextContains("")
        composeTestRule.onNodeWithContentDescription("Clear").assertDoesNotExist()
    }

    @Test
    fun onSearchAddress_triggersCallback() {
        var capturedAddress: String? = null
        composeTestRule.setContent {
            SearchAddressSection(
                shouldFocusSearchBar = false,
                onSearchAddress = { capturedAddress = it }
            )
        }

        composeTestRule.onNodeWithText("Search address").performTextInput("400 Bizzell St.")
        composeTestRule.onNodeWithText("Search address").performImeAction()

        assertEquals("400 Bizzell St.", capturedAddress)
    }

    @Test
    fun shouldFocusSearchBarTrue_requestsFocus() {
        composeTestRule.setContent {
            SearchAddressSection(shouldFocusSearchBar = true)
        }

        composeTestRule.onNodeWithText("Search address").assertIsFocused()
    }

    @Test
    fun shouldFocusSearchBarFalse_doesNotRequestFocus() {
        composeTestRule.setContent {
            SearchAddressSection(shouldFocusSearchBar = false)
        }

        composeTestRule.onNodeWithText("Search address").assertIsNotFocused()
    }
}
