package com.drew654.mocklocations.domain.model

import android.net.Uri

data class MockLocationsUiState(
    val savedCameraPosition: SavedCameraPosition? = null,
    val isMapCenteredAfterLaunch: Boolean = false,
    val importUri: Uri? = null,
    val controlsAreExpanded: Boolean = false,
    val speedUnitValue: SpeedUnitValue = SpeedUnitValue(30.0, SpeedUnit.MilesPerHour),
    val shouldFocusSearchBar: Boolean = false,
)
