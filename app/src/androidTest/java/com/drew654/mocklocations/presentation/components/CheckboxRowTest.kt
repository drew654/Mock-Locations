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

class CheckboxRowTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun checkboxRow_updatesStateCorrectly() {
        var checked by mutableStateOf(false)

        composeTestRule.setContent {
            CheckboxRow(
                label = "Checkbox",
                checked = checked,
                onCheckedChange = { checked = it }
            )
        }

        composeTestRule
            .onNodeWithText("Checkbox")
            .assertExists()
            .performClick()

        assertTrue(checked)

        composeTestRule
            .onNodeWithText("Checkbox")
            .performClick()

        assertFalse(checked)
    }
}
