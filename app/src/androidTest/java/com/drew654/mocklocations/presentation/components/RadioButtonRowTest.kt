package com.drew654.mocklocations.presentation.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class RadioButtonRowTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun radioButtonRow_updatesStateCorrectly() {
        var selected by mutableStateOf(false)

        composeTestRule.setContent {
            RadioButtonRow(
                label = "Radio button",
                selected = selected,
                onClick = { selected = !selected }
            )
        }

        composeTestRule
            .onNodeWithText("Radio button")
            .assertExists()
            .performClick()

        assertTrue(selected)

        composeTestRule
            .onNodeWithText("Radio button")
            .performClick()

        assertFalse(selected)
    }
}
