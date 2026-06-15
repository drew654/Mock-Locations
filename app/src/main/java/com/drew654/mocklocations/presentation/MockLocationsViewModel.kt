package com.drew654.mocklocations.presentation

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.ExpandedControlsState
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MapState
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.Permission
import com.drew654.mocklocations.domain.model.RouteSegment
import com.drew654.mocklocations.domain.model.SavedCameraPosition
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import com.drew654.mocklocations.domain.model.isGranted
import com.drew654.mocklocations.repository.RouteRepository
import com.drew654.mocklocations.service.MockLocationService
import com.drew654.mocklocations.service.MockLocationService.Companion.ACTION_RESTORE_STRAIGHT_LINE_MOCKING
import com.drew654.mocklocations.service.MockLocationService.Companion.ACTION_START_MOCKING
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MockLocationsViewModel @Inject constructor(
    application: Application,
    private val settingsManager: SettingsManager,
    private val routeRepository: RouteRepository
) : AndroidViewModel(application) {
    private val _uiMapState = MutableStateFlow(MapState())
    val mapState: StateFlow<MapState> = combine(
        _uiMapState,
        settingsManager.mockControlStateFlow,
        settingsManager.currentMockedLocationFlow
    ) { uiMapState, mockControlState, currentMockedLocation ->
        uiMapState.copy(
            mockControlState = mockControlState,
            currentMockedLocation = currentMockedLocation
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MapState()
    )

    init {
        viewModelScope.launch {
            combine(
                settingsManager.speedUnitValueFlow,
                settingsManager.speedSliderLowerEndFlow,
                settingsManager.speedSliderUpperEndFlow
            ) { speedUnitValue, lowerEnd, upperEnd ->
                Triple(speedUnitValue, lowerEnd, upperEnd)
            }.distinctUntilChanged().collect { (speedUnitValue, lowerEnd, upperEnd) ->
                updateExpandedControlsState {
                    it.copy(
                        speedUnitValue = speedUnitValue,
                        speedSliderLowerEnd = lowerEnd,
                        speedSliderUpperEnd = upperEnd
                    )
                }
            }
        }

        viewModelScope.launch {
            val currentState = settingsManager.mockControlStateFlow.first()
            settingsManager.setMockControlState(currentState.copy(isWaitingForRouteFetch = false))
        }

        viewModelScope.launch {
            val wasMocking = settingsManager.mockControlStateFlow.first().isMocking
            val activeLocationTarget = settingsManager.mockControlStateFlow.first().activeLocationTarget
            if (wasMocking) {
                Intent(application, MockLocationService::class.java).apply {
                    action =
                        if (activeLocationTarget.isRoute()) {
                            ACTION_RESTORE_STRAIGHT_LINE_MOCKING
                        } else {
                            ACTION_START_MOCKING
                        }
                }.also {
                    application.startForegroundService(it)
                }
            }
        }

        val filter = IntentFilter(MockLocationService.ACTION_ROUTE_FINISHED)
        ContextCompat.registerReceiver(application, object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                viewModelScope.launch {
                    val clearRouteOnStop = settingsManager.clearRouteOnStopFlow.first()
                    updateMockControlState {
                        it.copy(
                            isMocking = false,
                            isPaused = false,
                            isWaitingAtEndOfRoute = false,
                            activeLocationTarget = if (clearRouteOnStop) LocationTarget.Empty else (it.activeLocationTarget)
                        )
                    }
                }
            }
        }, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    fun updateCameraPosition(position: CameraPosition) {
        updateMapState {
            it.copy(
                savedCameraPosition = SavedCameraPosition(
                    latitude = position.target.latitude,
                    longitude = position.target.longitude,
                    zoom = position.zoom
                )
            )
        }
    }

    fun setMapIsCenteredAfterLaunch() {
        updateMapState { it.copy(isMapCenteredAfterLaunch = true) }
    }

    fun updateMapState(transform: (MapState) -> MapState) {
        val currentState = _uiMapState.value
        val newState = transform(currentState)
        _uiMapState.value = newState
    }

    fun refreshMapState(
        context: Context,
        isSystemInDarkTheme: Boolean
    ) {
        viewModelScope.launch {
            val mapStyle = settingsManager.mapStyleFlow.first()
            val hasLocationPermission = Permission.FineLocation.isGranted(context)
            val isCameraFollowingMockedLocation = settingsManager.isCameraFollowingMockedLocation.first()
            val isCameraCurrentlyFollowingMockedLocation = settingsManager.isCameraCurrentlyFollowingMockedLocationFlow.first()
            val speedUnitValue = settingsManager.speedUnitValueFlow.first()
            val speedSliderLowerEnd = settingsManager.speedSliderLowerEndFlow.first()
            val speedSliderUpperEnd = settingsManager.speedSliderUpperEndFlow.first()
            val mapProperties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                isBuildingEnabled = true,
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                    context,
                    mapStyle?.resourceId
                        ?: if (isSystemInDarkTheme) {
                            R.raw.map_style_night
                        } else {
                            R.raw.map_style_standard
                        }
                ),
                mapType = mapStyle?.mapType ?: MapType.NORMAL
            )
            val savedRoutes = settingsManager.savedRoutesFlow.first()
            updateMapState {
                it.copy(
                    mapStyle = mapStyle,
                    isCameraFollowingMockedLocation = isCameraFollowingMockedLocation,
                    isCameraCurrentlyFollowingMockedLocation = isCameraCurrentlyFollowingMockedLocation,
                    hasLocationPermission = hasLocationPermission,
                    mapProperties = mapProperties,
                    savedRoutes = savedRoutes,
                    expandedControlsState = it.expandedControlsState.copy(
                        speedUnitValue = speedUnitValue,
                        speedSliderLowerEnd = speedSliderLowerEnd,
                        speedSliderUpperEnd = speedSliderUpperEnd
                    )
                )
            }
        }
    }

    fun updateExpandedControlsState(transform: (ExpandedControlsState) -> ExpandedControlsState) {
        updateMapState { mapState ->
            val currentState = mapState.expandedControlsState
            val newState = transform(currentState)
            mapState.copy(expandedControlsState = newState)
        }
    }

    fun setControlsAreExpanded(expanded: Boolean) {
        updateExpandedControlsState { it.copy(isExpanded = expanded) }
    }

    private suspend fun updateMockControlState(transform: (MockControlState) -> MockControlState) {
        val currentState = settingsManager.mockControlStateFlow.first()
        val newState = transform(currentState)
        settingsManager.setMockControlState(newState)
    }

    fun togglePause() {
        viewModelScope.launch {
            updateMockControlState { it.copy(isPaused = !it.isPaused) }
        }
    }

    suspend fun pushRouteSegment(point: LatLng) {
        val isBuildRoutesOnRoad = settingsManager.buildRouteOnRoadsFlow.first()
        if (isBuildRoutesOnRoad) {
            if (mapState.value.mockControlState.activeLocationTarget is LocationTarget.Empty) {
                updateMockControlState {
                    it.copy(
                        activeLocationTarget = LocationTarget.create(
                            listOf(
                                RouteSegment(listOf(point))
                            )
                        )
                    )
                }
            } else {
                fetchAndAppendRoute(
                    start = mapState.value.mockControlState.activeLocationTarget.getLastPoint()!!,
                    end = point
                )
            }
        } else {
            updateMockControlState { it.copy(activeLocationTarget = LocationTarget.create(it.activeLocationTarget.routeSegments + RouteSegment(listOf(point)))) }
        }
    }

    fun popRouteSegment() {
        viewModelScope.launch {
            updateMockControlState { it.copy(activeLocationTarget = LocationTarget.create(it.activeLocationTarget.routeSegments.dropLast(1))) }
        }
    }

    fun clearLocationTarget() {
        viewModelScope.launch {
            updateMockControlState { it.copy(activeLocationTarget = LocationTarget.Empty) }
        }
    }

    fun setShouldFocusSearchBar(value: Boolean) {
        updateMapState { it.copy(shouldFocusSearchBar = value) }
    }

    fun setSpeedValue(newSpeed: Double) {
        updateExpandedControlsState {
            it.copy(speedUnitValue = it.speedUnitValue.copy(value = newSpeed))
        }
    }

    fun startMockLocation(context: Context, pushPoint: LatLng? = null) {
        viewModelScope.launch {
            updateMockControlState { state ->
                val target = if (pushPoint == null) {
                    state.activeLocationTarget
                } else {
                    LocationTarget.create(state.activeLocationTarget.routeSegments + RouteSegment(listOf(pushPoint)))
                }
                state.copy(isMocking = true, activeLocationTarget = target)
            }

            val intent = Intent(getApplication(), MockLocationService::class.java).apply {
                action = ACTION_START_MOCKING
            }
            context.startForegroundService(intent)
        }
    }

    fun stopMockLocation() {
        viewModelScope.launch {
            val isClearRouteOnStop = settingsManager.clearRouteOnStopFlow.first()
            updateMockControlState {
                it.copy(
                    isMocking = false,
                    isPaused = false,
                    isWaitingAtEndOfRoute = false,
                    activeLocationTarget = if (isClearRouteOnStop) LocationTarget.Empty else (it.activeLocationTarget)
                )
            }
        }
        val intent = Intent(getApplication(), MockLocationService::class.java).apply {
            action = MockLocationService.ACTION_STOP_MOCKING
        }
        getApplication<Application>().startService(intent)
    }

    fun fetchAndAppendRoute(start: LatLng, end: LatLng) {
        viewModelScope.launch {
            updateMockControlState { it.copy(isWaitingForRouteFetch = true) }
            val points = routeRepository.getRoutePoints(start, end)
            if (points.isNotEmpty()) {
                updateMockControlState {
                    it.copy(
                        activeLocationTarget = LocationTarget.create(
                            it.activeLocationTarget.routeSegments + RouteSegment(points)
                        ),
                        isWaitingForRouteFetch = false
                    )
                }
            } else {
                Toast.makeText(getApplication(), "No route found", Toast.LENGTH_SHORT).show()
                updateMockControlState { it.copy(isWaitingForRouteFetch = false) }
            }
        }
    }

    fun saveCurrentRoute(name: String) {
        val current = mapState.value.mockControlState.activeLocationTarget
        if (current.routeSegments.isNotEmpty()) {
            val routeToSave = LocationTarget.SavedRoute(name = name, routeSegments = current.routeSegments)
            viewModelScope.launch {
                settingsManager.saveRoute(route = routeToSave)
                val newRoutes = settingsManager.savedRoutesFlow.first()
                updateMapState {
                    it.copy(
                        savedRoutes = newRoutes
                    )
                }
            }
        }
    }

    fun loadSavedRoute(route: LocationTarget.SavedRoute) {
        viewModelScope.launch {
            updateMockControlState { it.copy(activeLocationTarget = route) }
        }
    }

    fun deleteSavedRoute(route: LocationTarget.SavedRoute) {
        viewModelScope.launch {
            settingsManager.deleteRoute(route)
            val newRoutes = settingsManager.savedRoutesFlow.first()
            updateMapState {
                it.copy(
                    savedRoutes = newRoutes
                )
            }
        }
    }

    fun saveSpeedUnitValue(speedUnitValue: SpeedUnitValue) {
        viewModelScope.launch {
            settingsManager.setSpeedUnitValue(speedUnitValue)
        }
    }

    fun setIsCameraCurrentlyFollowingMockedLocation(value: Boolean) {
        viewModelScope.launch {
            settingsManager.setIsCameraCurrentlyFollowingMockedLocation(value)
        }
    }
}
