package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SearchAddressButtonTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun isShowingSearch_false_showsSearchIcon() {
        composeTestRule.setContent {
            SearchAddressButton(
                isShowingSearch = false
            )
        }

        composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
    }

    @Test
    fun isShowingSearch_true_showsCloseSearchIcon() {
        composeTestRule.setContent {
            SearchAddressButton(
                isShowingSearch = true
            )
        }

        composeTestRule.onNodeWithContentDescription("Close search").assertIsDisplayed()
    }

    @Test
    fun clickButton_whenNotShowingSearch_callsSetSearchWithTrue() {
        var calledWith: Boolean? = null
        composeTestRule.setContent {
            SearchAddressButton(
                isShowingSearch = false,
                setShowSearch = { calledWith = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Search").performClick()

        assertEquals(true, calledWith)
    }

    @Test
    fun clickButton_whenShowingSearch_callsSetSearchWithFalse() {
        var calledWith: Boolean? = null
        composeTestRule.setContent {
            SearchAddressButton(
                isShowingSearch = true,
                setShowSearch = { calledWith = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Close search").performClick()

        assertEquals(false, calledWith)
    }
}
