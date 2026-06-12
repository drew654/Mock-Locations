package com.drew654.mocklocations.presentation.map_screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import com.drew654.mocklocations.domain.model.CompassState
import com.drew654.mocklocations.domain.model.ExpandedControlsState
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MapState
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.Permission
import com.drew654.mocklocations.domain.model.RouteSegment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MapScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val route1 = LocationTarget.SavedRoute(
        name = "Route 1",
        routeSegments = listOf(
            RouteSegment(
                points = listOf(
                    LatLng(0.0, 0.0)
                )
            ),
            RouteSegment(
                points = listOf(
                    LatLng(0.0, 0.1)
                )
            )
        )
    )

    private val route2 = LocationTarget.SavedRoute(
        name = "Route 2",
        routeSegments = listOf(
            RouteSegment(
                points = listOf(
                    LatLng(0.0, 0.0)
                )
            ),
            RouteSegment(
                points = listOf(
                    LatLng(1.0, 0.1)
                )
            )
        )
    )

    @Test
    fun clickSearchKey_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapContent(
                state = MapState(
                    isShowingSearch = true
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                onSearchAddress = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Search address").performTextInput("400 Bizzell St.")
        composeTestRule.onNodeWithText("Search address").performImeAction()

        assertTrue(clicked)
    }

    @Test
    fun clickExpandControls_triggersCallback() {
        var capturedValue: Boolean? = null
        composeTestRule.setContent {
            MapContent(
                state = MapState(),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
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
            MapContent(
                state = MapState(
                    expandedControlsState = ExpandedControlsState(
                        isExpanded = true
                    )
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                setControlsAreExpanded = { capturedValue = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Collapse controls").performClick()

        assertEquals(false, capturedValue)
    }

    @Test
    fun expandedControls_visible_whenStateVisible() {
        composeTestRule.setContent {
            MapContent(
                state = MapState(
                    expandedControlsState = ExpandedControlsState(
                        isExpanded = true
                    )
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState()
            )
        }

        composeTestRule.onNodeWithText("30").assertIsDisplayed()
        composeTestRule.onNodeWithText(" mph").assertIsDisplayed()
    }

    @Test
    fun expandedControls_notVisible_whenStateNotVisible() {
        composeTestRule.setContent {
            MapContent(
                state = MapState(),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState()
            )
        }

        composeTestRule.onNodeWithText("30").assertDoesNotExist()
    }

    @Test
    fun clickClearLocationTarget_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapContent(
                state = MapState(
                    mockControlState = MockControlState(
                        activeLocationTarget = route1
                    )
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                onClearLocationTarget = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Clear").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickStart_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapContent(
                state = MapState(),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
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
            MapContent(
                state = MapState(
                    mockControlState = MockControlState(
                        isMocking = true,
                        activeLocationTarget = route1
                    )
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                onStop = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Stop").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickPopRouteSegment_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapContent(
                state = MapState(
                    mockControlState = MockControlState(
                        activeLocationTarget = route1
                    )
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                onPopRouteSegment = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Pop").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickPause_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapContent(
                state = MapState(
                    mockControlState = MockControlState(
                        isMocking = true,
                        activeLocationTarget = route1
                    )
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
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
            MapContent(
                state = MapState(
                    mockControlState = MockControlState(
                        isMocking = true,
                        activeLocationTarget = route1,
                        isPaused = true
                    )
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                onTogglePause = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Resume").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickSavedRoutes_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapContent(
                state = MapState(
                    mockControlState = MockControlState(
                        activeLocationTarget = route1
                    )
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                onSaveLocationTarget = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Saved routes").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickAddPoint_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapContent(
                state = MapState(),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                onAddCrosshairsPoint = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Add point").performClick()

        assertTrue(clicked)
    }

    @Test
    fun crosshairs_visible_whenNotMocking() {
        composeTestRule.setContent {
            MapContent(
                state = MapState(),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState()
            )
        }

        composeTestRule.onNodeWithTag(testTag = "crosshairs", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun crosshairs_notVisible_whenMocking() {
        composeTestRule.setContent {
            MapContent(
                state = MapState(
                    mockControlState = MockControlState(
                        isMocking = true,
                        activeLocationTarget = route1
                    )
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState()
            )
        }

        composeTestRule.onNodeWithTag("crosshairs").assertDoesNotExist()
    }

    @Test
    fun crosshairs_notVisible_whenDisabled() {
        composeTestRule.setContent {
            MapContent(
                state = MapState(
                    mockControlState = MockControlState(
                        isUsingCrosshairs = false
                    )
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState()
            )
        }

        composeTestRule.onNodeWithTag("crosshairs").assertDoesNotExist()
    }

    @Test
    fun clickMyLocation_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapContent(
                state = MapState(),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                onUserLocationFocus = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("My location").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickSearch_triggersCallback() {
        var capturedValue: Boolean? = null
        composeTestRule.setContent {
            MapContent(
                state = MapState(),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                onShowSearch = { capturedValue = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Search").performClick()

        assertEquals(true, capturedValue)
    }

    @Test
    fun clickCloseSearch_triggersCallback() {
        var capturedValue: Boolean? = null
        composeTestRule.setContent {
            MapContent(
                state = MapState(
                    isShowingSearch = true
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                onShowSearch = { capturedValue = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Close search", useUnmergedTree = true).performScrollTo().performClick()

        assertEquals(false, capturedValue)
    }

    @Test
    fun clickSaveRoute_triggersSetIsNamingRouteCallback() {
        var capturedValue: Boolean? = null
        composeTestRule.setContent {
            MapContent(
                state = MapState(
                    mockControlState = MockControlState(
                        activeLocationTarget = route1
                    ),
                    isShowingSavedRoutesDialog = true
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                onSetIsNamingRoute = { capturedValue = it }
            )
        }

        composeTestRule.onNodeWithText("Save route").performClick()

        assertEquals(true, capturedValue)
    }

    @Test
    fun clickCancel_triggersSetIsNamingRouteCallback() {
        var capturedValue: Boolean? = null
        composeTestRule.setContent {
            MapContent(
                state = MapState(
                    isShowingSavedRoutesDialog = true
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                onSetIsNamingRoute = { capturedValue = it }
            )
        }

        composeTestRule.onNodeWithText("Cancel").performClick()

        assertEquals(false, capturedValue)
    }

    @Test
    fun clickCancel_triggersOnDismissSavedRouteDialogCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapContent(
                state = MapState(
                    isShowingSavedRoutesDialog = true
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                onDismissSavedRouteDialog = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Cancel").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickSaveRoute_triggersOnRouteSavedCallback() {
        var routeName: String? = null
        composeTestRule.setContent {
            MapContent(
                state = MapState(
                    isShowingSavedRoutesDialog = true,
                    isNamingRoute = true
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                onRouteSaved = { routeName = it }
            )
        }

        composeTestRule.onNodeWithText("Route name").performTextInput("Route 1")
        composeTestRule.onNodeWithText("Save route").performClick()

        assertEquals("Route 1", routeName)
    }

    @Test
    fun clickRoute_triggersOnRouteLoadedCallback() {
        var loadedRoute: LocationTarget.SavedRoute? = null
        composeTestRule.setContent {
            MapContent(
                state = MapState(
                    mockControlState = MockControlState(),
                    savedRoutes = listOf(route1, route2),
                    isShowingSavedRoutesDialog = true
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                onRouteLoaded = { loadedRoute = it }
            )
        }

        composeTestRule.onNodeWithText("Route 1").performClick()

        assertEquals(route1, loadedRoute)
    }

    @Test
    fun clickDelete_triggersOnRouteDeletedCallback() {
        var deletedRoute: LocationTarget.SavedRoute? = null
        composeTestRule.setContent {
            MapContent(
                state = MapState(
                    savedRoutes = listOf(route1, route2),
                    isShowingSavedRoutesDialog = true
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                onRouteDeleted = { deletedRoute = it }
            )
        }

        composeTestRule.onNodeWithText("Route 1").performTouchInput { longClick() }
        composeTestRule.onNodeWithText("Delete selected").performClick()

        assertEquals(route1, deletedRoute)
    }

    @Test
    fun clickCancel_triggersOnDismissPermissionsDialogCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapContent(
                state = MapState(
                    permissionToBeRequested = Permission.FineLocation
                ),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                onDismissPermissionsDialog = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Cancel").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickCompass_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapContent(
                state = MapState(),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(
                    bearing = { 90f }
                ),
                onClickCompass = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Align north").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickSettings_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MapContent(
                state = MapState(),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
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
            MapContent(
                state = MapState(),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
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
            MapContent(
                state = MapState(),
                cameraPositionState = CameraPositionState(),
                compassState = CompassState(),
                onZoomOut = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Zoom out").performClick()

        assertTrue(clicked)
    }
}
