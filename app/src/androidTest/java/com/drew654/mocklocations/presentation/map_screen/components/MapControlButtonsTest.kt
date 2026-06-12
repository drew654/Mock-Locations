package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.drew654.mocklocations.domain.model.CompassState
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.RouteSegment
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MapControlButtonsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val route = LocationTarget.Route(
        routeSegments = listOf(
            RouteSegment(
                points = listOf(
                    LatLng(1.0, 0.0)
                )
            ),
            RouteSegment(
                points = listOf(
                    LatLng(0.1, 0.5)
                )
            )
        )
    )

    @Test
    fun clickStart_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapControlButtons(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground,
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
            MapControlButtons(
                mockControlState = MockControlState(
                    isMocking = true,
                    activeLocationTarget = route
                ),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground,
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
            MapControlButtons(
                mockControlState = MockControlState(
                    isMocking = true,
                    activeLocationTarget = route
                ),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground,
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
            MapControlButtons(
                mockControlState = MockControlState(
                    isMocking = true,
                    activeLocationTarget = route,
                    isPaused = true
                ),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground,
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
            MapControlButtons(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground,
                onAddCrosshairsPoint = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Add point").performClick()

        assertTrue(clicked)
    }

    @Test
    fun crosshairs_visible_whenNotMocking() {
        composeTestRule.setContent {
            MapControlButtons(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground
            )
        }

        composeTestRule.onNodeWithTag("crosshairs").assertIsDisplayed()
    }

    @Test
    fun crosshairs_notVisible_whenMocking() {
        composeTestRule.setContent {
            MapControlButtons(
                mockControlState = MockControlState(
                    isMocking = true
                ),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground
            )
        }

        composeTestRule.onNodeWithTag("crosshairs").assertDoesNotExist()
    }

    @Test
    fun crosshairs_notVisible_whenDisabled() {
        composeTestRule.setContent {
            MapControlButtons(
                mockControlState = MockControlState(
                    isUsingCrosshairs = false
                ),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground
            )
        }

        composeTestRule.onNodeWithTag("crosshairs").assertDoesNotExist()
    }

    @Test
    fun clickPopRouteSegment_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapControlButtons(
                mockControlState = MockControlState(
                    activeLocationTarget = route
                ),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground,
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
            MapControlButtons(
                mockControlState = MockControlState(
                    activeLocationTarget = route
                ),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground,
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
            MapControlButtons(
                mockControlState = MockControlState(
                    activeLocationTarget = route
                ),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground,
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
            MapControlButtons(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground,
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
            MapControlButtons(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = true,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground,
                setShowSearch = { capturedValue = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Close search").performClick()

        assertEquals(false, capturedValue)
    }

    @Test
    fun clickExpandControls_triggersCallback() {
        var capturedValue: Boolean? = null
        composeTestRule.setContent {
            MapControlButtons(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground,
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
            MapControlButtons(
                mockControlState = MockControlState(),
                controlsAreExpanded = true,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground,
                setControlsAreExpanded = { capturedValue = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Collapse controls").performClick()

        assertEquals(false, capturedValue)
    }

    @Test
    fun clickMyLocation_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapControlButtons(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground,
                onUserLocationFocus = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("My location").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickCompass_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapControlButtons(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(
                    bearing = { 90f }
                ),
                crosshairsColor = MaterialTheme.colorScheme.onBackground,
                onClickCompass = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Align north").performClick()

        assertTrue(clicked)
    }

    @Test
    fun compass_notVisible_whenMapIsFacingNorth() {
        composeTestRule.setContent {
            MapControlButtons(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground
            )
        }

        composeTestRule.onNodeWithContentDescription("Align north").assertDoesNotExist()
    }

    @Test
    fun clickSettings_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapControlButtons(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground,
                onSettingsClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Settings").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickZoomIn_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapControlButtons(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground,
                onZoomIn = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Zoom in").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickZoomOut_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapControlButtons(
                mockControlState = MockControlState(),
                controlsAreExpanded = false,
                isShowingSearch = false,
                compassState = CompassState(),
                crosshairsColor = MaterialTheme.colorScheme.onBackground,
                onZoomOut = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Zoom out").performClick()

        assertTrue(clicked)
    }
}
