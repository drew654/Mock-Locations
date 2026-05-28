package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.drew654.mocklocations.R
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class DisableableFloatingActionButtonTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickButton_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            DisableableFloatingActionButton(
                onClick = { clicked = true }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                    contentDescription = "Start"
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Start").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickButton_disabled_whenStateDisabled() {
        var clicked = false
        composeTestRule.setContent {
            DisableableFloatingActionButton(
                onClick = { clicked = true },
                enabled = false
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                    contentDescription = "Start"
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Start").performClick()

        assertFalse(clicked)
    }
}
