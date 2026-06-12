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
    fun `Stop is visible and enabled when mocking`() {
        val mockingState = MockControlState(
            isMocking = true,
            isPaused = false,
            isWaitingAtEndOfRoute = false,
            activeLocationTarget = trigonToMacRoute,
            isUsingCrosshairs = false,
            isWaitingForRouteFetch = false
        )

        assertTrue(mockingState.isStopVisible())
        assertTrue(mockingState.isStopEnabled())
    }

    @Test
    fun `Stop is not visible or enabled when not mocking`() {
        val state1 = MockControlState(
            isMocking = false,
            isPaused = false,
            isWaitingAtEndOfRoute = false,
            activeLocationTarget = LocationTarget.Empty,
            isUsingCrosshairs = false,
            isWaitingForRouteFetch = false
        )
        val state2 = MockControlState(
            isMocking = false,
            isPaused = false,
            isWaitingAtEndOfRoute = false,
            activeLocationTarget = LocationTarget.Empty,
            isUsingCrosshairs = true,
            isWaitingForRouteFetch = false
        )
        val state3 = MockControlState(
            activeLocationTarget = trigonToMacRoute,
            isMocking = false,
            isPaused = false,
            isUsingCrosshairs = true,
            isWaitingAtEndOfRoute = false,
            isWaitingForRouteFetch = true
        )
        val state4 = MockControlState(
            isMocking = false,
            isPaused = false,
            isWaitingAtEndOfRoute = false,
            activeLocationTarget = trigonToMacRoute,
            isUsingCrosshairs = true,
            isWaitingForRouteFetch = false
        )

        assertFalse(state1.isStopVisible())
        assertFalse(state1.isStopEnabled())
        assertFalse(state2.isStopVisible())
        assertFalse(state2.isStopEnabled())
        assertFalse(state3.isStopVisible())
        assertFalse(state3.isStopEnabled())
        assertFalse(state4.isStopVisible())
        assertFalse(state4.isStopEnabled())
    }

    @Test
    fun `Pause and Resume visibility and enabled check for routes`() {
        val mockingState = MockControlState(
            isMocking = true,
            isPaused = false,
            isWaitingAtEndOfRoute = false,
            activeLocationTarget = trigonToMacRoute,
            isUsingCrosshairs = false,
            isWaitingForRouteFetch = false
        )

        assertTrue(mockingState.isPauseVisible())
        assertTrue(mockingState.isPauseEnabled())
        assertFalse(mockingState.isResumeVisible())
        assertFalse(mockingState.isResumeEnabled())

        val pausedState = mockingState.copy(isPaused = true)

        assertFalse(pausedState.isPauseVisible())
        assertFalse(pausedState.isPauseEnabled())
        assertTrue(pausedState.isResumeVisible())
        assertTrue(pausedState.isResumeEnabled())
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

        assertTrue(state.isAddPointVisible())
        assertFalse(state.isAddPointEnabled())
        assertFalse(state.isLongPressAddPointEnabled())
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

    @Test
    fun `Pop Point is always visible`() {
        assertTrue(isPopPointVisible())
    }

    @Test
    fun `Pop Point is enabled when building a route`() {
        val state = MockControlState(
            isMocking = false,
            isPaused = false,
            isWaitingAtEndOfRoute = false,
            activeLocationTarget = trigonToMacRoute,
            isUsingCrosshairs = true,
            isWaitingForRouteFetch = false
        )

        assertTrue(state.isPopPointEnabled())
    }

    @Test
    fun `Clear Location Target is always visible`() {
        assertTrue(isClearLocationTargetVisible())
    }

    @Test
    fun `Clear Location Target is enabled when building a route`() {
        val state = MockControlState(
            isMocking = false,
            isPaused = false,
            isWaitingAtEndOfRoute = false,
            activeLocationTarget = trigonToMacRoute,
            isUsingCrosshairs = true,
            isWaitingForRouteFetch = false
        )

        assertTrue(state.isClearLocationTargetEnabled())
    }
}
