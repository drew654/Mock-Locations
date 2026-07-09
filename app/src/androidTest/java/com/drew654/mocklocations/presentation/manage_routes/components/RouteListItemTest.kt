package com.drew654.mocklocations.presentation.manage_routes.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.RouteSegment
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RouteListItemTest {
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

    @Test
    fun displaysRouteName() {
        composeTestRule.setContent {
            RouteListItem(
                route = route1,
                speedUnit = SpeedUnit.MilesPerHour
            )
        }

        composeTestRule.onNodeWithText("Route 1").assertIsDisplayed()
    }

    @Test
    fun displaysRouteDetails() {
        composeTestRule.setContent {
            RouteListItem(
                route = route1,
                speedUnit = SpeedUnit.MilesPerHour
            )
        }

        composeTestRule.onNodeWithText("2 points", substring = true).assertIsDisplayed()
    }

    @Test
    fun speedUnitKph_displaysMetricDistance() {
        composeTestRule.setContent {
            RouteListItem(
                route = route1,
                speedUnit = SpeedUnit.KilometersPerHour
            )
        }

        composeTestRule.onNodeWithText("km", substring = true).assertIsDisplayed()
    }

    @Test
    fun speedUnitMps_displaysMetricDistance() {
        composeTestRule.setContent {
            RouteListItem(
                route = route1,
                speedUnit = SpeedUnit.MetersPerSecond
            )
        }

        composeTestRule.onNodeWithText("km", substring = true).assertIsDisplayed()
    }

    @Test
    fun speedUnitMph_displaysImperialDistance() {
        composeTestRule.setContent {
            RouteListItem(
                route = route1,
                speedUnit = SpeedUnit.MilesPerHour
            )
        }

        composeTestRule.onNodeWithText("mi", substring = true).assertIsDisplayed()
    }

    @Test
    fun click_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            RouteListItem(
                route = route1,
                speedUnit = SpeedUnit.MilesPerHour,
                onClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Route 1").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickEdit_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            RouteListItem(
                route = route1,
                speedUnit = SpeedUnit.MilesPerHour,
                isExpanded = true,
                onEditClicked = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Edit name").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickCopy_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            RouteListItem(
                route = route1,
                speedUnit = SpeedUnit.MilesPerHour,
                isExpanded = true,
                onCopyClicked = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Copy").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickDelete_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            RouteListItem(
                route = route1,
                speedUnit = SpeedUnit.MilesPerHour,
                isExpanded = true,
                onDeleteClicked = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Delete").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickUp_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            RouteListItem(
                route = route1,
                speedUnit = SpeedUnit.MilesPerHour,
                isExpanded = true,
                onUpClicked = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Move up").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickDown_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            RouteListItem(
                route = route1,
                speedUnit = SpeedUnit.MilesPerHour,
                isExpanded = true,
                onDownClicked = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Move down").performClick()

        assertTrue(clicked)
    }
}
