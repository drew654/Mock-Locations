package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.RouteSegment
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PrimaryMockLocationControlsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val point = LocationTarget.SinglePoint(
        point = LatLng(0.0, 0.0)
    )

    private val route = LocationTarget.Route(
        routeSegments = listOf(
            RouteSegment(
                points = listOf(
                    LatLng(0.0, 0.0)
                )
            ),
            RouteSegment(
                points = listOf(
                    LatLng(1.0, 1.0)
                )
            )
        )
    )

    @Test
    fun defaultState_showsAddPointAndStartButtons() {
        composeTestRule.setContent {
            PrimaryMockLocationControls(
                mockControlState = MockControlState()
            )
        }

        composeTestRule.onNodeWithContentDescription("Add point").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Start").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Stop").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Pause").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Resume").assertDoesNotExist()
    }

    @Test
    fun mockingState_showsStopButton() {
        composeTestRule.setContent {
            PrimaryMockLocationControls(
                mockControlState = MockControlState(
                    isMocking = true,
                    activeLocationTarget = point
                )
            )
        }

        composeTestRule.onNodeWithContentDescription("Add point").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Start").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Stop").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Pause").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Resume").assertDoesNotExist()
    }

    @Test
    fun mockingRouteState_showsStopAndPauseButtons() {
        composeTestRule.setContent {
            PrimaryMockLocationControls(
                mockControlState = MockControlState(
                    isMocking = true,
                    activeLocationTarget = route
                )
            )
        }

        composeTestRule.onNodeWithContentDescription("Add point").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Start").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Stop").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Pause").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Resume").assertDoesNotExist()
    }

    @Test
    fun pausedRouteState_showsStopAndResumeButtons() {
        composeTestRule.setContent {
            PrimaryMockLocationControls(
                mockControlState = MockControlState(
                    isMocking = true,
                    activeLocationTarget = route,
                    isPaused = true
                )
            )
        }

        composeTestRule.onNodeWithContentDescription("Add point").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Start").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Stop").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Pause").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Resume").assertIsDisplayed()
    }

    @Test
    fun waitingAtEndOfRouteState_showsStopButton() {
        composeTestRule.setContent {
            PrimaryMockLocationControls(
                mockControlState = MockControlState(
                    isMocking = true,
                    activeLocationTarget = route,
                    isWaitingAtEndOfRoute = true
                )
            )
        }

        composeTestRule.onNodeWithContentDescription("Add point").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Start").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Stop").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Pause").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Resume").assertDoesNotExist()
    }

    @Test
    fun clickState_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            PrimaryMockLocationControls(
                mockControlState = MockControlState(),
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
            PrimaryMockLocationControls(
                mockControlState = MockControlState(isMocking = true),
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
            PrimaryMockLocationControls(
                mockControlState = MockControlState(
                    isMocking = true,
                    activeLocationTarget = route
                ),
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
            PrimaryMockLocationControls(
                mockControlState = MockControlState(
                    isMocking = true,
                    activeLocationTarget = route,
                    isPaused = true
                ),
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
            PrimaryMockLocationControls(
                mockControlState = MockControlState(
                    isMocking = false
                ),
                onAddCrosshairsPoint = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Add point").performClick()

        assertTrue(clicked)
    }
}
