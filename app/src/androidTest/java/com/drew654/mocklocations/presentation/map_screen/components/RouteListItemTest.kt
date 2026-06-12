package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
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
                selected = false,
                shouldShowCheckbox = false,
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
                selected = false,
                shouldShowCheckbox = false,
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
                selected = false,
                shouldShowCheckbox = false,
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
                selected = false,
                shouldShowCheckbox = false,
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
                selected = false,
                shouldShowCheckbox = false,
                speedUnit = SpeedUnit.MilesPerHour
            )
        }

        composeTestRule.onNodeWithText("mi", substring = true).assertIsDisplayed()
    }

    @Test
    fun checkboxHidden_whenStateIsHidden() {
        composeTestRule.setContent {
            RouteListItem(
                route = route1,
                selected = false,
                shouldShowCheckbox = false,
                speedUnit = SpeedUnit.MilesPerHour
            )
        }

        composeTestRule.onNode(isToggleable()).assertDoesNotExist()
    }

    @Test
    fun checkboxChecked_whenSelectedIsTrue() {
        composeTestRule.setContent {
            RouteListItem(
                route = route1,
                selected = true,
                shouldShowCheckbox = true,
                speedUnit = SpeedUnit.MilesPerHour
            )
        }

        composeTestRule.onNode(isToggleable()).assertIsOn()
    }

    @Test
    fun checkboxUnchecked_whenSelectedIsFalse() {
        composeTestRule.setContent {
            RouteListItem(
                route = route1,
                selected = false,
                shouldShowCheckbox = true,
                speedUnit = SpeedUnit.MilesPerHour
            )
        }

        composeTestRule.onNode(isToggleable()).assertIsOff()
    }

    @Test
    fun click_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            RouteListItem(
                route = route1,
                selected = false,
                shouldShowCheckbox = false,
                speedUnit = SpeedUnit.MilesPerHour,
                onClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Route 1").performClick()

        assertTrue(clicked)
    }

    @Test
    fun longClick_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            RouteListItem(
                route = route1,
                selected = false,
                shouldShowCheckbox = false,
                speedUnit = SpeedUnit.MilesPerHour,
                onLongClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Route 1").performTouchInput { longClick() }

        assertTrue(clicked)
    }
}
