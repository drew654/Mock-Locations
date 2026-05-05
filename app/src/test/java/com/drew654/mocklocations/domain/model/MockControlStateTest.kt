package com.drew654.mocklocations.domain.model

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test

class MockControlStateTest {
    val trigonToMacRoute = LocationTarget.SavedRoute(
        name = "Trigon to MAC",
        routeSegments = listOf(
            RouteSegment(points = listOf(LatLng(30.613716193675007, -96.33953779935837))),
            RouteSegment(points = listOf(LatLng(30.61254844344945, -96.33846625685692))),
            RouteSegment(points = listOf(LatLng(30.61186111760771, -96.33693136274815))),
            RouteSegment(points = listOf(LatLng(30.609536523531006, -96.33468501269817)))
        )
    )

    @Test
    fun `default values are correct`() {
        val state = MockControlState()

        assertFalse("Should not be mocking by default", state.isMocking)
        assertFalse("Should not be paused by default", state.isPaused)
        assertFalse("Should not be waiting at end of route by default", state.isWaitingAtEndOfRoute)
        assertEquals("Active location target should be Empty", LocationTarget.Empty, state.activeLocationTarget)
        assertTrue("Crosshairs should be enabled by default", state.isUsingCrosshairs)
        assertFalse("Should not be waiting for route fetch by default", state.isWaitingForRouteFetch)
    }

    @Test
    fun `copy method correctly updates specific fields`() {
        val initialState = MockControlState()

        val updatedState = initialState.copy(
            isMocking = true,
            isPaused = true,
            isUsingCrosshairs = false
        )

        assertTrue(updatedState.isMocking)
        assertTrue(updatedState.isPaused)
        assertFalse(updatedState.isUsingCrosshairs)
        assertFalse(updatedState.isWaitingAtEndOfRoute)
        assertEquals(LocationTarget.Empty, updatedState.activeLocationTarget)
    }

    @Test
    fun `equality works for different instances with same values`() {
        val state1 = MockControlState(isMocking = true, isPaused = false)
        val state2 = MockControlState(isMocking = true, isPaused = false)
        val state3 = MockControlState(isMocking = false, isPaused = false)

        assertEquals("Instances with same values should be equal", state1, state2)
        assertEquals("Hashcodes should match for equal instances", state1.hashCode(), state2.hashCode())
        assertNotEquals("Instances with different values should not be equal", state1, state3)
        assertNotEquals("Hashcodes should not match for instances with different values", state1.hashCode(), state3.hashCode())
    }

    @Test
    fun `isStartEnabled should be false when location target is empty and crosshairs are off`() {
        val state = MockControlState(
            isMocking = false,
            isPaused = false,
            isWaitingAtEndOfRoute = false,
            activeLocationTarget = LocationTarget.Empty,
            isUsingCrosshairs = false,
            isWaitingForRouteFetch = false
        )

        assertFalse(state.isStartEnabled())
    }

    @Test
    fun `isStartEnabled should be true when using crosshairs even if location target is empty`() {
        val state = MockControlState(
            isMocking = false,
            isPaused = false,
            isWaitingAtEndOfRoute = false,
            activeLocationTarget = LocationTarget.Empty,
            isUsingCrosshairs = true,
            isWaitingForRouteFetch = false
        )

        assertTrue(state.isStartEnabled())
    }

    @Test
    fun `Pause and Resume visibility for routes`() {
        val mockingState = MockControlState(
            isMocking = true,
            isPaused = false,
            isWaitingAtEndOfRoute = false,
            activeLocationTarget = trigonToMacRoute,
            isUsingCrosshairs = false,
            isWaitingForRouteFetch = false
        )
        assertTrue(mockingState.isPauseVisible())
        assertFalse(mockingState.isResumeVisible())

        val pausedState = mockingState.copy(isPaused = true)
        assertFalse(pausedState.isPauseVisible())
        assertTrue(pausedState.isResumeVisible())
    }

    @Test
    fun `Add Point is disabled during route fetch`() {
        val state = MockControlState(
            activeLocationTarget = trigonToMacRoute,
            isMocking = false,
            isPaused = false,
            isUsingCrosshairs = true,
            isWaitingAtEndOfRoute = false,
            isWaitingForRouteFetch = true
        )

        assertTrue("Add point should be visible", state.isAddPointVisible())
        assertFalse("Add point should be disabled while fetching", state.isAddPointEnabled())
    }

    @Test
    fun `Pop Point and Clear are disabled when mocking`() {
        val state = MockControlState(
            isMocking = true,
            isPaused = false,
            isWaitingAtEndOfRoute = false,
            activeLocationTarget = trigonToMacRoute,
            isUsingCrosshairs = false,
            isWaitingForRouteFetch = false
        )

        assertFalse(state.isPopPointEnabled())
        assertFalse(state.isClearLocationTargetEnabled())
    }
}
