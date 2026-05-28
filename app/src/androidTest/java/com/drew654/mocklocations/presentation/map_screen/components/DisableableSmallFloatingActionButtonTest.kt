package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.drew654.mocklocations.R
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class DisableableSmallFloatingActionButtonTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickButton_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            DisableableSmallFloatingActionButton(
                onClick = { clicked = true }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_pause_24),
                    contentDescription = "Pause",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Pause").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickButton_disabled_whenStateDisabled() {
        var clicked = false
        composeTestRule.setContent {
            DisableableSmallFloatingActionButton(
                onClick = { clicked = true },
                enabled = false
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_pause_24),
                    contentDescription = "Pause",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Pause").performClick()

        assertFalse(clicked)
    }
}
