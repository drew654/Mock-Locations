package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.RouteSegment
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class RoutesListDialogBodyTest {
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
    fun emptyList_showsEmptyMessage() {
        composeTestRule.setContent {
            RoutesListDialogBody(
                savedRoutes = emptyList(),
                selectedRoutes = emptyList(),
                isSaveRouteEnabled = false,
                speedUnit = SpeedUnit.MilesPerHour
            )
        }

        composeTestRule.onNodeWithText("No saved routes found").assertIsDisplayed()
    }

    @Test
    fun hasRoutes_showsRouteNames() {
        composeTestRule.setContent {
            RoutesListDialogBody(
                savedRoutes = listOf(route1, route2),
                selectedRoutes = emptyList(),
                isSaveRouteEnabled = false,
                speedUnit = SpeedUnit.MilesPerHour
            )
        }

        composeTestRule.onNodeWithText("Route 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Route 2").assertIsDisplayed()
    }

    @Test
    fun clickRoute_triggersOnRouteLoaded_whenNoneSelected() {
        var loadedRoute: LocationTarget.SavedRoute? = null
        composeTestRule.setContent {
            RoutesListDialogBody(
                savedRoutes = listOf(route1),
                selectedRoutes = emptyList(),
                isSaveRouteEnabled = false,
                speedUnit = SpeedUnit.MilesPerHour,
                onRouteLoaded = { loadedRoute = it }
            )
        }

        composeTestRule.onNodeWithText("Route 1").performClick()

        assertEquals(route1, loadedRoute)
    }

    @Test
    fun longClickRoute_triggersOnRouteSelected() {
        var selectedRoute: LocationTarget.SavedRoute? = null
        composeTestRule.setContent {
            RoutesListDialogBody(
                savedRoutes = listOf(route1),
                selectedRoutes = emptyList(),
                isSaveRouteEnabled = false,
                speedUnit = SpeedUnit.MilesPerHour,
                onRouteSelected = { selectedRoute = it }
            )
        }

        composeTestRule.onNodeWithText("Route 1").performTouchInput { longClick() }

        assertEquals(route1, selectedRoute)
    }

    @Test
    fun clickSelectedRoute_triggersOnRouteDeselected() {
        var deselectedRoute: LocationTarget.SavedRoute? = null
        composeTestRule.setContent {
            RoutesListDialogBody(
                savedRoutes = listOf(route1),
                selectedRoutes = listOf(route1),
                isSaveRouteEnabled = false,
                speedUnit = SpeedUnit.MilesPerHour,
                onRouteDeselected = { deselectedRoute = it }
            )
        }

        composeTestRule.onNodeWithText("Route 1").performClick()

        assertEquals(route1, deselectedRoute)
    }

    @Test
    fun clickUnselectedRoute_triggersOnRouteSelected_whenOtherRouteSelected() {
        var selectedRoute: LocationTarget.SavedRoute? = null
        composeTestRule.setContent {
            RoutesListDialogBody(
                savedRoutes = listOf(route1, route2),
                selectedRoutes = listOf(route1),
                isSaveRouteEnabled = false,
                speedUnit = SpeedUnit.MilesPerHour,
                onRouteSelected = { selectedRoute = it }
            )
        }

        composeTestRule.onNodeWithText("Route 2").performClick()

        assertEquals(route2, selectedRoute)
    }

    @Test
    fun clickDeleteSelected_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            RoutesListDialogBody(
                savedRoutes = listOf(route1),
                selectedRoutes = listOf(route1),
                isSaveRouteEnabled = false,
                speedUnit = SpeedUnit.MilesPerHour,
                onDeleteSelectedRoutes = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Delete selected").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickCancel_triggersOnClearSelectedRoutes_whenRouteSelected() {
        var clicked = false
        composeTestRule.setContent {
            RoutesListDialogBody(
                savedRoutes = listOf(route1),
                selectedRoutes = listOf(route1),
                isSaveRouteEnabled = false,
                speedUnit = SpeedUnit.MilesPerHour,
                onClearSelectedRoutes = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Cancel").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickCancel_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            RoutesListDialogBody(
                savedRoutes = emptyList(),
                selectedRoutes = emptyList(),
                isSaveRouteEnabled = false,
                speedUnit = SpeedUnit.MilesPerHour,
                onDismiss = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Cancel").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickSaveRoute_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            RoutesListDialogBody(
                savedRoutes = emptyList(),
                selectedRoutes = emptyList(),
                isSaveRouteEnabled = true,
                speedUnit = SpeedUnit.MilesPerHour,
                onConfirm = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Save route").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickSaveRoute_disabled_whenSaveRouteDisabled() {
        var clicked = false
        composeTestRule.setContent {
            RoutesListDialogBody(
                savedRoutes = emptyList(),
                selectedRoutes = emptyList(),
                isSaveRouteEnabled = false,
                speedUnit = SpeedUnit.MilesPerHour,
                onConfirm = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Save route").performClick()

        assertFalse(clicked)
    }
}
