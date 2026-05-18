package com.drew654.mocklocations.domain.model

import android.net.Uri

data class MockLocationsUiState(
    val savedCameraPosition: SavedCameraPosition? = null,
    val isMapCenteredAfterLaunch: Boolean = false,
    val importUri: Uri? = null,
    val shouldFocusSearchBar: Boolean = false,
    val expandedControlsState: ExpandedControlsState = ExpandedControlsState()
)
