package com.drew654.mocklocations.domain.model

import org.junit.Assert.*
import org.junit.Test

class SettingsStateTest {
    @Test
    fun `data class properties are correctly initialized`() {
        val isBuildRouteOnRoads = true
        val isUsingCrosshairs = false
        val clearPointsOnStop = false
        val isCameraFollowingMockedLocation = false
        val isGoingToWaitAtRouteFinish = true
        val mapStyle = MapStyle.Hybrid
        val locationAccuracyLevel = LocationAccuracyLevel.Medium
        val locationUpdateDelay = 2.5f
        val isShowingMapStyleDialog = true
        val isShowingLocationAccuracyLevelDialog = true
        val isShowingLocationUpdateDelayDialog = true
        val isShowingResetSettingsDialog = true

        val settingsState = SettingsState(
            isBuildRouteOnRoads = isBuildRouteOnRoads,
            isUsingCrosshairs = isUsingCrosshairs,
            clearPointsOnStop = clearPointsOnStop,
            isCameraFollowingMockedLocation = isCameraFollowingMockedLocation,
            isGoingToWaitAtRouteFinish = isGoingToWaitAtRouteFinish,
            mapStyle = mapStyle,
            locationAccuracyLevel = locationAccuracyLevel,
            locationUpdateDelay = locationUpdateDelay,
            isShowingMapStyleDialog = isShowingMapStyleDialog,
            isShowingLocationAccuracyLevelDialog = isShowingLocationAccuracyLevelDialog,
            isShowingLocationUpdateDelayDialog = isShowingLocationUpdateDelayDialog,
            isShowingResetSettingsDialog = isShowingResetSettingsDialog
        )

        assertEquals(isBuildRouteOnRoads, settingsState.isBuildRouteOnRoads)
        assertEquals(isUsingCrosshairs, settingsState.isUsingCrosshairs)
        assertEquals(clearPointsOnStop, settingsState.clearPointsOnStop)
        assertEquals(isCameraFollowingMockedLocation, settingsState.isCameraFollowingMockedLocation)
        assertEquals(isGoingToWaitAtRouteFinish, settingsState.isGoingToWaitAtRouteFinish)
        assertEquals(mapStyle, settingsState.mapStyle)
        assertEquals(locationAccuracyLevel, settingsState.locationAccuracyLevel)
        assertEquals(locationUpdateDelay, settingsState.locationUpdateDelay)
        assertEquals(isShowingMapStyleDialog, settingsState.isShowingMapStyleDialog)
        assertEquals(isShowingLocationAccuracyLevelDialog, settingsState.isShowingLocationAccuracyLevelDialog)
        assertEquals(isShowingLocationUpdateDelayDialog, settingsState.isShowingLocationUpdateDelayDialog)
        assertEquals(isShowingResetSettingsDialog, settingsState.isShowingResetSettingsDialog)
    }
}
