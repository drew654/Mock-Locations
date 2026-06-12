package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.RouteSegment
import com.google.android.gms.maps.model.LatLng
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test

class MockLocationControlsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val route = LocationTarget.Route(
        routeSegments = listOf(
            RouteSegment(
                points = listOf(
                    LatLng(0.0, 0.0)
                )
            ),
            RouteSegment(
                points = listOf(
                    LatLng(1.0, 1.1)
                )
            )
        )
    )

    @Test
    fun displaysPrimaryAndSecondaryControls() {
        composeTestRule.setContent {
            MockLocationControls(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = false
            )
        }

        composeTestRule.onNodeWithContentDescription("Start").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
    }

    @Test
    fun clickExpandControls_triggersCallback() {
        var capturedValue: Boolean? = null
        composeTestRule.setContent {
            MockLocationControls(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = false,
                setControlsAreExpanded = { capturedValue = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Expand controls").performClick()

        assertEquals(true, capturedValue)
    }

    @Test
    fun clickCollapseControls_triggersCallback() {
        var capturedValue: Boolean? = null
        composeTestRule.setContent {
            MockLocationControls(
                mockControlState = MockControlState(),
                controlsAreExpanded = true,
                isShowingSearch = false,
                setControlsAreExpanded = { capturedValue = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Collapse controls").performClick()

        assertEquals(false, capturedValue)
    }

    @Test
    fun clickStart_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MockLocationControls(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = false,
                onStart = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Start").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickStop_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MockLocationControls(
                mockControlState = MockControlState(
                    isMocking = true,
                    activeLocationTarget = route
                ),
                controlsAreExpanded = false,
                isShowingSearch = false,
                onStop = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Stop").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickPause_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MockLocationControls(
                mockControlState = MockControlState(
                    isMocking = true,
                    activeLocationTarget = route
                ),
                controlsAreExpanded = false,
                isShowingSearch = false,
                onTogglePause = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Pause").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickResume_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MockLocationControls(
                mockControlState = MockControlState(
                    isMocking = true,
                    activeLocationTarget = route,
                    isPaused = true
                ),
                controlsAreExpanded = false,
                isShowingSearch = false,
                onTogglePause = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Resume").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickAddPoint_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MockLocationControls(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = false,
                onAddCrosshairsPoint = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Add point").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickPopRouteSegment_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MockLocationControls(
                mockControlState = MockControlState(
                    activeLocationTarget = route
                ),
                controlsAreExpanded = false,
                isShowingSearch = false,
                onPopRouteSegment = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Pop").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickClearLocationTarget_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MockLocationControls(
                mockControlState = MockControlState(
                    activeLocationTarget = route
                ),
                controlsAreExpanded = false,
                isShowingSearch = false,
                onClearLocationTarget = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Clear").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickSavedRoutes_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MockLocationControls(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = false,
                onSaveLocationTarget = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Saved routes").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickSearch_triggersCallback() {
        var capturedValue: Boolean? = null
        composeTestRule.setContent {
            MockLocationControls(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = false,
                setShowSearch = { capturedValue = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Search").performClick()

        assertEquals(true, capturedValue)
    }

    @Test
    fun clickCloseSearch_triggersCallback() {
        var capturedValue: Boolean? = null
        composeTestRule.setContent {
            MockLocationControls(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = true,
                setShowSearch = { capturedValue = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Close search").performClick()

        assertEquals(false, capturedValue)
    }
}
