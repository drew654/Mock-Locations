package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MockControlState
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SecondaryMockLocationControlsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val point = LocationTarget.SinglePoint(
        point = LatLng(0.0, 0.0)
    )

    @Test
    fun allButtonsAreDisplayed() {
        composeTestRule.setContent {
            SecondaryMockLocationControls(
                mockControlState = MockControlState(),
                isShowingSearch = false,
                scrollState = ScrollState(0)
            )
        }

        composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Saved routes").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Clear").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Pop").assertIsDisplayed()
    }

    @Test
    fun clickSearch_triggersCallback() {
        var calledWith: Boolean? = null
        composeTestRule.setContent {
            SecondaryMockLocationControls(
                mockControlState = MockControlState(),
                isShowingSearch = false,
                scrollState = ScrollState(0),
                setShowSearch = { calledWith = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Search").performClick()

        assertEquals(true, calledWith)
    }

    @Test
    fun clickSavedRoutes_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            SecondaryMockLocationControls(
                mockControlState = MockControlState(),
                isShowingSearch = false,
                scrollState = ScrollState(0),
                onSaveLocationTarget = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Saved routes").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickClear_triggersCallback_whenEnabled() {
        var clicked = false
        composeTestRule.setContent {
            SecondaryMockLocationControls(
                mockControlState = MockControlState(
                    activeLocationTarget = point,
                    isMocking = false
                ),
                isShowingSearch = false,
                scrollState = ScrollState(0),
                onClearLocationTarget = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Clear").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickClear_DoesNotTriggerCallback_whenDisabled() {
        var clicked = false
        composeTestRule.setContent {
            SecondaryMockLocationControls(
                mockControlState = MockControlState(),
                isShowingSearch = false,
                scrollState = ScrollState(0),
                onClearLocationTarget = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Clear").performClick()

        assertFalse(clicked)
    }

    @Test
    fun clickPop_triggersCallback_whenEnabled() {
        var clicked = false
        composeTestRule.setContent {
            SecondaryMockLocationControls(
                mockControlState = MockControlState(
                    activeLocationTarget = point
                ),
                isShowingSearch = false,
                scrollState = ScrollState(0),
                onPopRouteSegment = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Pop").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickPop_doesNotTriggerCallbackWhenDisabled() {
        var clicked = false
        composeTestRule.setContent {
            SecondaryMockLocationControls(
                mockControlState = MockControlState(),
                isShowingSearch = false,
                scrollState = ScrollState(0),
                onPopRouteSegment = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Pop").performClick()

        assertFalse(clicked)
    }
}
