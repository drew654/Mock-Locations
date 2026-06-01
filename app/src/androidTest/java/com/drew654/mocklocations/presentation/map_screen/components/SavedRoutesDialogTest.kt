package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.RouteSegment
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SavedRoutesDialogTest {
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
    fun dialog_notVisible_whenIsVisibleFalse() {
        composeTestRule.setContent {
            SavedRoutesDialog(
                isVisible = false,
                isNamingRoute = false,
                savedRoutes = emptyList(),
                locationTarget = LocationTarget.Empty,
                isMocking = false,
                speedUnit = SpeedUnit.MilesPerHour
            )
        }

        composeTestRule.onNodeWithText("Saved Routes").assertDoesNotExist()
    }

    @Test
    fun dialog_showsRoutesList_whenIsNamingRouteFalse() {
        composeTestRule.setContent {
            SavedRoutesDialog(
                isVisible = true,
                isNamingRoute = false,
                savedRoutes = emptyList(),
                locationTarget = LocationTarget.Empty,
                isMocking = false,
                speedUnit = SpeedUnit.MilesPerHour
            )
        }

        composeTestRule.onNodeWithText("Saved Routes").assertIsDisplayed()
    }

    @Test
    fun dialog_showsNamingRoute_whenIsNamingRouteTrue() {
        composeTestRule.setContent {
            SavedRoutesDialog(
                isVisible = true,
                isNamingRoute = true,
                savedRoutes = emptyList(),
                locationTarget = LocationTarget.Empty,
                isMocking = false,
                speedUnit = SpeedUnit.MilesPerHour
            )
        }

        composeTestRule.onNodeWithText("Save Route").assertIsDisplayed()
    }

    @Test
    fun clickSaveRoute_triggersOnSetIsNamingRoute() {
        var clicked = false
        composeTestRule.setContent {
            SavedRoutesDialog(
                isVisible = true,
                isNamingRoute = false,
                savedRoutes = emptyList(),
                locationTarget = route1,
                isMocking = false,
                speedUnit = SpeedUnit.MilesPerHour,
                onSetIsNamingRoute = { clicked = it }
            )
        }

        composeTestRule.onNodeWithText("Save route").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickCancel_triggersOnDismiss() {
        var clicked = false
        composeTestRule.setContent {
            SavedRoutesDialog(
                isVisible = true,
                isNamingRoute = false,
                savedRoutes = emptyList(),
                locationTarget = LocationTarget.Empty,
                isMocking = false,
                speedUnit = SpeedUnit.MilesPerHour,
                onDismiss = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Cancel").performClick()

        assertTrue(clicked)

    }

    @Test
    fun clickRoute_triggersOnRouteLoaded() {
        var loadedRoute: LocationTarget.SavedRoute? = null
        composeTestRule.setContent {
            SavedRoutesDialog(
                isVisible = true,
                isNamingRoute = false,
                savedRoutes = listOf(route1, route2),
                locationTarget = LocationTarget.Empty,
                isMocking = false,
                speedUnit = SpeedUnit.MilesPerHour,
                onRouteLoaded = { loadedRoute = it }
            )
        }

        composeTestRule.onNodeWithText("Route 1").performClick()

        assertEquals(route1, loadedRoute)
    }

    @Test
    fun longClickRoute_selectsRoute() {
        composeTestRule.setContent {
            SavedRoutesDialog(
                isVisible = true,
                isNamingRoute = false,
                savedRoutes = listOf(route1, route2),
                locationTarget = LocationTarget.Empty,
                isMocking = false,
                speedUnit = SpeedUnit.MilesPerHour
            )
        }

        composeTestRule.onNodeWithText("Route 1").performTouchInput { longClick() }

        composeTestRule.onNodeWithText("Delete selected").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save route").assertDoesNotExist()
    }

    @Test
    fun deleteSelectedRoutes_triggersOnRouteDeleted() {
        var deletedRoute: LocationTarget.SavedRoute? = null
        composeTestRule.setContent {
            SavedRoutesDialog(
                isVisible = true,
                isNamingRoute = false,
                savedRoutes = listOf(route1),
                locationTarget = LocationTarget.Empty,
                isMocking = false,
                speedUnit = SpeedUnit.MilesPerHour,
                onRouteDeleted = { deletedRoute = it }
            )
        }

        composeTestRule.onNodeWithText("Route 1").performTouchInput { longClick() }
        composeTestRule.onNodeWithText("Delete selected").performClick()

        assertEquals(route1, deletedRoute)
    }

    @Test
    fun namingRoute_saveTriggersOnRouteSaved() {
        var routeName: String? = null
        composeTestRule.setContent {
            SavedRoutesDialog(
                isVisible = true,
                isNamingRoute = true,
                savedRoutes = emptyList(),
                locationTarget = LocationTarget.Empty,
                isMocking = false,
                speedUnit = SpeedUnit.MilesPerHour,
                onRouteSaved = { routeName = it }
            )
        }

        composeTestRule.onNodeWithText("Route name").performTextInput("New Route")
        composeTestRule.onNodeWithText("Save route").performClick()

        assertEquals("New Route", routeName)
    }
}
