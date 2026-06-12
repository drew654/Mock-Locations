package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import com.drew654.mocklocations.domain.model.ExpandedControlsState
import org.junit.Rule
import org.junit.Test

class ExpandedControlsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun controlsAreVisibleWhenStateIsVisible() {
        composeTestRule.setContent {
            ExpandedControls(
                state = ExpandedControlsState(isExpanded = true)
            )
        }

        composeTestRule.onNodeWithText("30").assertIsDisplayed()
        composeTestRule.onNodeWithText(" mph").assertIsDisplayed()
    }

    @Test
    fun controlsAreNotVisibleWhenStateIsNotVisible() {
        composeTestRule.setContent {
            ExpandedControls(
                state = ExpandedControlsState(isExpanded = false)
            )
        }

        composeTestRule.onRoot().onChildren().assertCountEquals(0)
    }
}
