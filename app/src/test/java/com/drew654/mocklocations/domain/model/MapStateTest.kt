package com.drew654.mocklocations.domain.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import junit.framework.TestCase.assertEquals
import org.junit.Test

class MapStateTest {
    @Test
    fun `data class properties are correctly initialized`() {
        val mockControlState = MockControlState(
            isMocking = true,
            isPaused = true,
            isWaitingAtEndOfRoute = true,
            activeLocationTarget = LocationTarget.SinglePoint(
                point = LatLng(30.615165834851403, -96.34165674448013)
            ),
            isUsingCrosshairs = false,
            isWaitingForRouteFetch = true
        )
        val currentMockedLocation = RoutePoint(
            latLng = LatLng(30.615165834851403, -96.34165674448013),
            bearing = 90f
        )
        val savedCameraPosition = SavedCameraPosition(
            latitude = 30.615165834851403,
            longitude = -96.34165674448013,
            zoom = 15f
        )
        val isMapCenteredAfterLaunch = true
        val hasRestoredCamera = true
        val isCameraFollowingMockedLocation = false
        val isCameraCurrentlyFollowingMockedLocation = false
        val shouldFocusSearchBar = true
        val expandedControlsState = ExpandedControlsState(
            isExpanded = true,
            speedSliderLowerEnd = 50,
            speedSliderUpperEnd = 200,
            speedUnitValue = SpeedUnitValue(
                value = 50.0,
                speedUnit = SpeedUnit.KilometersPerHour
            )
        )
        val isShowingSearch = true
        val isShowingSavedRoutesDialog = true
        val isNamingRoute = true
        val permissionToBeRequested = Permission.PostNotifications
        val hasLocationPermission = true
        val mapStyle = MapStyle.Hybrid
        val mapUiSettings = MapUiSettings(
            compassEnabled = false,
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false
        )
        val mapProperties = MapProperties(
            isMyLocationEnabled = false,
            isBuildingEnabled = true,
            mapStyleOptions = null,
            mapType = MapType.NORMAL
        )
        val savedRoutes = listOf(
            LocationTarget.SavedRoute(
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
        )

        val mapState = MapState(
            mockControlState = mockControlState,
            currentMockedLocation = currentMockedLocation,
            savedCameraPosition = savedCameraPosition,
            isMapCenteredAfterLaunch = isMapCenteredAfterLaunch,
            hasRestoredCamera = hasRestoredCamera,
            isCameraFollowingMockedLocation = isCameraFollowingMockedLocation,
            isCameraCurrentlyFollowingMockedLocation = isCameraCurrentlyFollowingMockedLocation,
            shouldFocusSearchBar = shouldFocusSearchBar,
            expandedControlsState = expandedControlsState,
            isShowingSearch = isShowingSearch,
            isShowingSavedRoutesDialog = isShowingSavedRoutesDialog,
            isNamingRoute = isNamingRoute,
            permissionToBeRequested = permissionToBeRequested,
            hasLocationPermission = hasLocationPermission,
            mapStyle = mapStyle,
            mapUiSettings = mapUiSettings,
            mapProperties = mapProperties,
            savedRoutes = savedRoutes
        )

        assertEquals(mockControlState, mapState.mockControlState)
        assertEquals(currentMockedLocation, mapState.currentMockedLocation)
        assertEquals(savedCameraPosition, mapState.savedCameraPosition)
        assertEquals(isMapCenteredAfterLaunch, mapState.isMapCenteredAfterLaunch)
        assertEquals(hasRestoredCamera, mapState.hasRestoredCamera)
        assertEquals(isCameraFollowingMockedLocation, mapState.isCameraFollowingMockedLocation)
        assertEquals(isCameraCurrentlyFollowingMockedLocation, mapState.isCameraCurrentlyFollowingMockedLocation)
        assertEquals(shouldFocusSearchBar, mapState.shouldFocusSearchBar)
        assertEquals(expandedControlsState, mapState.expandedControlsState)
        assertEquals(isShowingSearch, mapState.isShowingSearch)
        assertEquals(isShowingSavedRoutesDialog, mapState.isShowingSavedRoutesDialog)
        assertEquals(isNamingRoute, mapState.isNamingRoute)
        assertEquals(permissionToBeRequested, mapState.permissionToBeRequested)
        assertEquals(hasLocationPermission, mapState.hasLocationPermission)
        assertEquals(mapStyle, mapState.mapStyle)
        assertEquals(mapUiSettings, mapState.mapUiSettings)
        assertEquals(mapProperties, mapState.mapProperties)
        assertEquals(savedRoutes, mapState.savedRoutes)
    }
}
