package com.drew654.mocklocations.domain.model

import androidx.compose.runtime.Immutable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings

@Immutable
data class MapState(
    val mockControlState: MockControlState = MockControlState(),
    val currentMockedLocation: RoutePoint? = null,
    val savedCameraPosition: SavedCameraPosition? = null,
    val isMapCenteredAfterLaunch: Boolean = false,
    val hasRestoredCamera: Boolean = false,
    val isCameraFollowingMockedLocation: Boolean = true,
    val isCameraCurrentlyFollowingMockedLocation: Boolean = true,
    val shouldFocusSearchBar: Boolean = false,
    val expandedControlsState: ExpandedControlsState = ExpandedControlsState(),
    val isShowingSearch: Boolean = false,
    val isShowingSavedRoutesDialog: Boolean = false,
    val isNamingRoute: Boolean = false,
    val permissionToBeRequested: Permission? = null,
    val hasLocationPermission: Boolean = false,
    val mapStyle: MapStyle? = null,
    val mapUiSettings: MapUiSettings = MapUiSettings(
        compassEnabled = false,
        myLocationButtonEnabled = false,
        zoomControlsEnabled = false
    ),
    val mapProperties: MapProperties = MapProperties(
        isMyLocationEnabled = false,
        isBuildingEnabled = true,
        mapStyleOptions = null,
        mapType = MapType.NORMAL
    ),
    val savedRoutes: List<LocationTarget.SavedRoute> = emptyList()
)
