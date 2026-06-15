package com.drew654.mocklocations.presentation.map_screen

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.ExpandedControlsState
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MapState
import com.drew654.mocklocations.domain.model.MapStyle
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.Permission
import com.drew654.mocklocations.domain.model.RoutePoint
import com.drew654.mocklocations.domain.model.RouteSegment
import com.drew654.mocklocations.domain.model.SavedCameraPosition
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import com.drew654.mocklocations.repository.RouteRepository
import com.drew654.mocklocations.service.MockLocationService
import com.drew654.mocklocations.service.MockLocationService.Companion.ACTION_RESTORE_STRAIGHT_LINE_MOCKING
import com.drew654.mocklocations.service.MockLocationService.Companion.ACTION_ROUTE_FINISHED
import com.drew654.mocklocations.service.MockLocationService.Companion.ACTION_START_MOCKING
import com.drew654.mocklocations.service.MockLocationService.Companion.ACTION_STOP_MOCKING
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.MapType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val application: Application,
    private val settingsManager: SettingsManager,
    private val routeRepository: RouteRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MapState())
    private val _isSystemInDarkTheme = MutableStateFlow(false)

    val state: StateFlow<MapState> = combine(
        _state,
        settingsManager.mockControlStateFlow,
        settingsManager.currentMockedLocationFlow,
        settingsManager.mapStyleFlow,
        settingsManager.isCameraFollowingMockedLocation,
        settingsManager.isCameraCurrentlyFollowingMockedLocationFlow,
        settingsManager.savedRoutesFlow,
        _isSystemInDarkTheme
    ) { params ->
        val state = params[0] as MapState
        val mockControlState = params[1] as MockControlState
        val currentMockedLocation = params[2] as RoutePoint?
        val mapStyle = params[3] as MapStyle?
        val isCameraFollowingMockedLocation = params[4] as Boolean
        val isCameraCurrentlyFollowingMockedLocation = params[5] as Boolean
        val savedRoutes = params[6] as List<LocationTarget.SavedRoute>
        val isDark = params[7] as Boolean

        val mapProperties = state.mapProperties.copy(
            isMyLocationEnabled = state.hasLocationPermission,
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                application,
                mapStyle?.resourceId
                    ?: if (isDark) {
                        R.raw.map_style_night
                    } else {
                        R.raw.map_style_standard
                    }
            ),
            mapType = mapStyle?.mapType ?: MapType.NORMAL
        )

        state.copy(
            mockControlState = mockControlState,
            currentMockedLocation = currentMockedLocation,
            mapStyle = mapStyle,
            isCameraFollowingMockedLocation = isCameraFollowingMockedLocation,
            isCameraCurrentlyFollowingMockedLocation = isCameraCurrentlyFollowingMockedLocation,
            savedRoutes = savedRoutes,
            mapProperties = mapProperties
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
            updateMockControlState { it.copy(isWaitingForRouteFetch = false) }
        }

        viewModelScope.launch {
            val wasMocking = settingsManager.mockControlStateFlow.first().isMocking
            val activeLocationTarget = settingsManager.mockControlStateFlow.first().activeLocationTarget
            if (wasMocking) {
                Intent(application, MockLocationService::class.java).apply {
                    action = if (activeLocationTarget.isRoute()) {
                        ACTION_RESTORE_STRAIGHT_LINE_MOCKING
                    } else {
                        ACTION_START_MOCKING
                    }
                }.also {
                    application.startForegroundService(it)
                }
            }
        }

        val filter = IntentFilter(ACTION_ROUTE_FINISHED)
        ContextCompat.registerReceiver(application, object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                viewModelScope.launch {
                    val clearRouteOnStop = settingsManager.clearRouteOnStopFlow.first()
                    updateMockControlState {
                        it.copy(
                            isMocking = false,
                            isPaused = false,
                            isWaitingAtEndOfRoute = false,
                            isWaitingForRouteFetch = false,
                            activeLocationTarget = if (clearRouteOnStop) LocationTarget.Empty else (it.activeLocationTarget)
                        )
                    }
                }
            }
        }, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    private suspend fun updateMockControlState(transform: (MockControlState) -> MockControlState) {
        val updatedState = _state.updateAndGet { state ->
            val currentState = state.mockControlState
            val newState = transform(currentState)
            state.copy(mockControlState = newState)
        }
        settingsManager.setMockControlState(updatedState.mockControlState)
    }

    fun clearLocationTarget() {
        viewModelScope.launch {
            updateMockControlState { it.copy(activeLocationTarget = LocationTarget.Empty) }
        }
    }

    fun popRouteSegment() {
        viewModelScope.launch {
            updateMockControlState { state ->
                state.copy(
                    activeLocationTarget = LocationTarget.create(state.activeLocationTarget.routeSegments.dropLast(1))
                )
            }
        }
    }

    fun togglePause() {
        viewModelScope.launch {
            updateMockControlState { state ->
                state.copy(
                    isPaused = !state.isPaused
                )
            }
        }
    }

    fun updateExpandedControlsState(transform: (ExpandedControlsState) -> ExpandedControlsState) {
        viewModelScope.launch {
            val updatedState = _state.updateAndGet { state ->
                val currentState = state.expandedControlsState
                val newState = transform(currentState)
                state.copy(expandedControlsState = newState)
            }
            settingsManager.setSpeedUnitValue(updatedState.expandedControlsState.speedUnitValue)
        }
    }

    fun setControlsAreExpanded(newValue: Boolean) {
        updateExpandedControlsState { it.copy(isExpanded = newValue) }
    }

    fun setSpeedValueUi(newValue: Double) {
        _state.update { state ->
            val currentState = state.expandedControlsState
            val currentSpeedUnitValue = state.expandedControlsState.speedUnitValue
            val newSpeedUnitValue = currentSpeedUnitValue.copy(value = newValue)
            val newState = currentState.copy(speedUnitValue = newSpeedUnitValue)
            state.copy(expandedControlsState = newState)
        }
    }

    fun setSpeedUnitValue(newValue: SpeedUnitValue) {
        updateExpandedControlsState { it.copy(speedUnitValue = newValue) }
    }

    fun setHasLocationPermission(newValue: Boolean) {
        _state.update { it.copy(hasLocationPermission = newValue) }
    }

    fun setPermissionToBeRequested(newValue: Permission?) {
        _state.update { it.copy(permissionToBeRequested = newValue) }
    }

    fun setCameraPosition(position: CameraPosition) {
        _state.update {
            it.copy(
                savedCameraPosition =
                    SavedCameraPosition(
                        latitude = position.target.latitude,
                        longitude = position.target.longitude,
                        zoom = position.zoom
                    )
            )
        }
    }

    fun setHasRestoredCamera(newValue: Boolean) {
        _state.update { it.copy(hasRestoredCamera = newValue) }
    }

    fun setIsCameraCurrentlyFollowingMockedLocation(newValue: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isCameraCurrentlyFollowingMockedLocation = newValue) }
            settingsManager.setIsCameraCurrentlyFollowingMockedLocation(newValue)
        }
    }

    fun setMapIsCenteredAfterLaunch() {
        _state.update { it.copy(isMapCenteredAfterLaunch = true) }
    }

    suspend fun pushRouteSegment(point: LatLng) {
        val isBuildRouteOnRoads = settingsManager.buildRouteOnRoadsFlow.first()
        if (isBuildRouteOnRoads) {
            if (_state.value.mockControlState.activeLocationTarget is LocationTarget.Empty) {
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
                    start = _state.value.mockControlState.activeLocationTarget.getLastPoint()!!,
                    end = point
                )
            }
        } else {
            updateMockControlState {
                it.copy(
                    activeLocationTarget = LocationTarget.create(
                        it.activeLocationTarget.routeSegments + RouteSegment(
                            listOf(point)
                        )
                    )
                )
            }
        }
    }

    private suspend fun fetchAndAppendRoute(start: LatLng, end: LatLng) {
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
            Toast.makeText(application, "No route found", Toast.LENGTH_SHORT).show()
            updateMockControlState { it.copy(isWaitingForRouteFetch = false) }
        }
    }

    fun startMockLocation(cameraPositionTarget: LatLng) {
        viewModelScope.launch {
            updateMockControlState { state ->
                val target = if (state.isUsingCrosshairs && state.activeLocationTarget is LocationTarget.Empty) {
                    LocationTarget.create(state.activeLocationTarget.routeSegments + RouteSegment(listOf(cameraPositionTarget)))
                } else {
                    state.activeLocationTarget
                }
                state.copy(isMocking = true, activeLocationTarget = target)
            }

            Intent(application, MockLocationService::class.java).apply {
                action = ACTION_START_MOCKING
            }.also {
                application.startForegroundService(it)
            }
        }
    }

    fun stopMockLocation() {
        viewModelScope.launch {
            val isClearRouteOnStop = settingsManager.clearRouteOnStopFlow.first()
            updateMockControlState { state ->
                state.copy(
                    isMocking = false,
                    isPaused = false,
                    isWaitingForRouteFetch = false,
                    activeLocationTarget = if (isClearRouteOnStop) LocationTarget.Empty else (state.activeLocationTarget)
                )
            }

            Intent(application, MockLocationService::class.java).apply {
                action = ACTION_STOP_MOCKING
            }.also {
                application.startService(it)
            }
        }
    }

    fun setIsShowingSavedRoutesDialog(newValue: Boolean) {
        _state.update { it.copy(isShowingSavedRoutesDialog = newValue) }
    }

    fun setIsSystemInDarkTheme(newValue: Boolean) {
        _isSystemInDarkTheme.value = newValue
    }

    fun setIsNamingRoute(newValue: Boolean) {
        _state.update { it.copy(isNamingRoute = newValue) }
    }

    fun setIsShowingSearch(newValue: Boolean) {
        _state.update { it.copy(isShowingSearch = newValue) }
    }

    fun saveCurrentRoute(name: String) {
        val current = _state.value.mockControlState.activeLocationTarget
        if (current.isRoute()) {
            val routeToSave =
                LocationTarget.SavedRoute(name = name, routeSegments = current.routeSegments)
            viewModelScope.launch {
                settingsManager.saveRoute(route = routeToSave)
                val newRoutes = settingsManager.savedRoutesFlow.first()
                _state.update { it.copy(savedRoutes = newRoutes) }
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
            _state.update { it.copy(savedRoutes = newRoutes) }
        }
    }
}
