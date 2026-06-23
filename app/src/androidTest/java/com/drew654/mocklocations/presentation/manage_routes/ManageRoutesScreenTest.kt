package com.drew654.mocklocations.presentation.manage_routes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.ManageRoutesState
import com.drew654.mocklocations.domain.model.RouteSegment
import com.google.android.gms.maps.model.LatLng
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ManageRoutesScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<ManageRoutesViewModel>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val state = MutableStateFlow(ManageRoutesState())

    private fun setupMockFlows() {
        state.value = ManageRoutesState()
        every { viewModel.state } returns state
    }

    private val route1 = LocationTarget.SavedRoute(
        name = "Route 1",
        routeSegments = listOf(
            RouteSegment(
                points = listOf(
                    LatLng(0.0, 0.0),
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
                    LatLng(0.0, 0.0),
                    LatLng(0.0, 0.12),
                    LatLng(0.0, 0.08)
                )
            )
        )
    )

    @Test
    fun clickBack_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ManageRoutesContent(
                state = ManageRoutesState(),
                onBackButtonClicked = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(clicked)
    }

    @Test
    fun screenDisplaysAllRoutes() {
        composeTestRule.setContent {
            ManageRoutesContent(
                state = ManageRoutesState(
                    routes = listOf(route1, route2)
                )
            )
        }

        listOf(route1, route2).forEach { route ->
            composeTestRule.onNodeWithText(route.name).assertIsDisplayed()
        }
    }

    @Test
    fun screenDisplaysAllRoutes_whenOneIsSelected() {
        composeTestRule.setContent {
            ManageRoutesContent(
                state = ManageRoutesState(
                    routes = listOf(route1, route2),
                    selectedIndex = 1
                )
            )
        }

        listOf(route1, route2).forEach { route ->
            composeTestRule.onNodeWithText(route.name).assertIsDisplayed()
        }
    }

    @Test
    fun clickRoute_triggersCallback() {
        var capturedRouteIndex: Int? = null
        composeTestRule.setContent {
            ManageRoutesContent(
                state = ManageRoutesState(
                    routes = listOf(route1, route2)
                ),
                onRouteSelected = { index ->
                    capturedRouteIndex = index
                }
            )
        }

        composeTestRule.onNodeWithText("Route 2").performClick()

        assertEquals(1, capturedRouteIndex)
    }

    @Test
    fun clickSelectedRoute_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ManageRoutesContent(
                state = ManageRoutesState(
                    routes = listOf(route1, route2),
                    selectedIndex = 0
                ),
                onRouteDeselected = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Route 1").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickUp_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ManageRoutesContent(
                state = ManageRoutesState(
                    routes = listOf(route1, route2),
                    selectedIndex = 1
                ),
                onRouteMovedUp = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Move up").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickDown_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ManageRoutesContent(
                state = ManageRoutesState(
                    routes = listOf(route1, route2),
                    selectedIndex = 0
                ),
                onRouteMovedDown = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Move down").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickCopy_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ManageRoutesContent(
                state = ManageRoutesState(
                    routes = listOf(route1, route2),
                    selectedIndex = 1
                ),
                onCopyRoute = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Copy").performClick()

        assertTrue(clicked)
    }

    @Test
    fun integration_clickBack_callsNavController() {
        setupMockFlows()

        composeTestRule.setContent {
            ManageRoutesScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        verify { navController.popBackStack() }
    }

    @Test
    fun integration_clickRoute_callsViewModel() {
        setupMockFlows()

        state.update { it.copy(routes = listOf(route1, route2)) }

        composeTestRule.setContent {
            ManageRoutesScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Route 1").performClick()

        verify { viewModel.setSelectedIndex(0) }
    }

    @Test
    fun integration_clickSelectedRoute_callsViewModel() {
        setupMockFlows()

        state.update { it.copy(routes = listOf(route1, route2), selectedIndex = 1) }

        composeTestRule.setContent {
            ManageRoutesScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Route 2").performClick()

        verify { viewModel.deselectRoute() }
    }

    @Test
    fun integration_clickUp_callsViewModel() {
        setupMockFlows()

        state.update { it.copy(routes = listOf(route1, route2), selectedIndex = 1) }

        composeTestRule.setContent {
            ManageRoutesScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithContentDescription("Move up").performClick()

        verify { viewModel.moveRouteUp() }
    }

    @Test
    fun integration_clickDown_callsViewModel() {
        setupMockFlows()

        state.update { it.copy(routes = listOf(route1, route2), selectedIndex = 0) }

        composeTestRule.setContent {
            ManageRoutesScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithContentDescription("Move down").performClick()

        verify { viewModel.moveRouteDown() }
    }

    @Test
    fun integration_clickCopy_callsViewModel() {
        setupMockFlows()

        state.update { it.copy(routes = listOf(route1, route2), selectedIndex = 1) }

        composeTestRule.setContent {
            ManageRoutesScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithContentDescription("Copy").performClick()

        verify { viewModel.copyRoute() }
    }
}
