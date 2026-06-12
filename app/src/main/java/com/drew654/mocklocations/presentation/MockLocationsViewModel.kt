package com.drew654.mocklocations.presentation

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.drew654.mocklocations.R
import com.drew654.mocklocations.repository.ExportRepository
import com.drew654.mocklocations.repository.RouteRepository
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.ExpandedControlsConfigurationState
import com.drew654.mocklocations.domain.model.ExpandedControlsState
import com.drew654.mocklocations.domain.model.ExportSettingsState
import com.drew654.mocklocations.domain.model.ImportRouteOption
import com.drew654.mocklocations.domain.model.ImportSettingsState
import com.drew654.mocklocations.domain.model.LocationAccuracyLevel
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MapState
import com.drew654.mocklocations.domain.model.MapStyle
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.Permission
import com.drew654.mocklocations.domain.model.RouteSegment
import com.drew654.mocklocations.domain.model.SavedCameraPosition
import com.drew654.mocklocations.domain.model.SettingsState
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import com.drew654.mocklocations.domain.model.isGranted
import com.drew654.mocklocations.service.MockLocationService
import com.drew654.mocklocations.service.MockLocationService.Companion.ACTION_RESTORE_STRAIGHT_LINE_MOCKING
import com.drew654.mocklocations.service.MockLocationService.Companion.ACTION_START_MOCKING
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val exportRepository: ExportRepository,
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
    private val _expandedControlsConfigurationState = MutableStateFlow(ExpandedControlsConfigurationState())
    val expandedControlsConfigurationState: StateFlow<ExpandedControlsConfigurationState> = _expandedControlsConfigurationState.asStateFlow()
    private val _exportSettingsState = MutableStateFlow(ExportSettingsState())
    val exportSettingsState: StateFlow<ExportSettingsState> = _exportSettingsState.asStateFlow()
    private val _importSettingsState = MutableStateFlow(ImportSettingsState())
    val importSettingsState: StateFlow<ImportSettingsState> = _importSettingsState.asStateFlow()
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

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

    fun exportDataToUri(uri: Uri, exportSettings: Boolean, exportRoutes: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>().applicationContext
                val jsonString = exportRepository.generateExportToJson(context, exportSettings, exportRoutes)
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                    outputStream.flush()
                }
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Export successful", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Export failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun importDataFromUri(importSettingsState: ImportSettingsState) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            try {
                val json = context.contentResolver
                    .openInputStream(_uiMapState.value.importUri!!)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: throw IllegalStateException("Unable to read file")

                exportRepository.importFromJson(json, importSettingsState.isImportSettings, importSettingsState.importRouteOption)
                val savedSpeedUnitValue = settingsManager.speedUnitValueFlow.first()
                updateExpandedControlsState { it.copy(speedUnitValue = savedSpeedUnitValue) }

                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Import successful", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun getVersionCodeFromUri(): Int {
        var versionCode = 0
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            try {
                val json = context.contentResolver
                    .openInputStream(_uiMapState.value.importUri!!)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: throw IllegalStateException("Unable to read file")
                versionCode = exportRepository.getVersionCodeFromJson(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return versionCode
    }

    fun getRouteCountFromImportUri(): Int {
        var count = 0
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            try {
                val json = context.contentResolver
                    .openInputStream(_uiMapState.value.importUri!!)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: throw IllegalStateException("Unable to read file")

                count = exportRepository.getRouteCountFromJson(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return count
    }

    fun getIsWithSettingsToImportFromImportUri(): Boolean {
        var isWithSettingsToImport = false
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            try {
                val json = context.contentResolver
                    .openInputStream(_uiMapState.value.importUri!!)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: throw IllegalStateException("Unable to read file")

                isWithSettingsToImport = exportRepository.isWithSettingsToImport(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return isWithSettingsToImport
    }

    fun resetSettingsToDefault() {
        viewModelScope.launch {
            settingsManager.resetToDefault()
            val savedSpeedUnitValue = settingsManager.speedUnitValueFlow.first()
            val savedSpeedSliderLowerEnd = settingsManager.speedSliderLowerEndFlow.first()
            val savedSpeedSliderUpperEnd = settingsManager.speedSliderUpperEndFlow.first()
            updateExpandedControlsState {
                it.copy(
                    speedUnitValue = savedSpeedUnitValue,
                    speedSliderLowerEnd = savedSpeedSliderLowerEnd,
                    speedSliderUpperEnd = savedSpeedSliderUpperEnd
                )
            }
            refreshSettingsState()
        }
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

    fun setImportUri(uri: Uri?) {
        updateMapState { it.copy(importUri = uri) }
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

    fun setClearRouteOnStop(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setClearRouteOnStop(enabled)
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

    fun refreshExportSettingsState() {
        viewModelScope.launch {
            val currentRoutesCount = settingsManager.savedRoutesFlow.first().size
            _exportSettingsState.value = ExportSettingsState(
                routesToExport = currentRoutesCount,
                isExportSettings = true,
                isExportRoutes = currentRoutesCount > 0
            )
        }
    }

    fun updateExportSettingsState(transform: (ExportSettingsState) -> ExportSettingsState) {
        _exportSettingsState.value = transform(_exportSettingsState.value)
    }

    fun refreshImportSettingsState() {
        val isImportSettingsEnabled = getIsWithSettingsToImportFromImportUri()
        val routesToImport = getRouteCountFromImportUri()
        val isImportRoutesEnabled = routesToImport > 0
        _importSettingsState.value = ImportSettingsState(
            isImportRoutesEnabled = isImportRoutesEnabled,
            isImportRoutes = isImportRoutesEnabled,
            isImportSettingsEnabled = isImportSettingsEnabled,
            isImportSettings = isImportSettingsEnabled,
            importRouteOption = if (isImportRoutesEnabled) ImportRouteOption.REPLACE else null,
            routesToImport = routesToImport
        )
    }

    fun updateImportSettingsState(transform: (ImportSettingsState) -> ImportSettingsState) {
        _importSettingsState.value = transform(_importSettingsState.value)
    }

    fun refreshSettingsState() {
        viewModelScope.launch {
            val isBuildRouteOnRoads = settingsManager.buildRouteOnRoadsFlow.first()
            val isUsingCrosshairs = settingsManager.mockControlStateFlow.first().isUsingCrosshairs
            val clearPointsOnStop = settingsManager.clearRouteOnStopFlow.first()
            val isCameraFollowingMockedLocation = settingsManager.isCameraFollowingMockedLocation.first()
            val isGoingToWaitAtRouteFinish = settingsManager.isGoingToWaitAtRouteFinishFlow.first()
            val mapStyle = settingsManager.mapStyleFlow.first()
            val locationAccuracyLevel = settingsManager.locationAccuracyLevelFlow.first()
            val locationUpdateDelay = settingsManager.locationUpdateDelayFlow.first()

            _settingsState.value = SettingsState(
                isBuildRouteOnRoads = isBuildRouteOnRoads,
                isUsingCrosshairs = isUsingCrosshairs,
                clearPointsOnStop = clearPointsOnStop,
                isCameraFollowingMockedLocation = isCameraFollowingMockedLocation,
                isGoingToWaitAtRouteFinish = isGoingToWaitAtRouteFinish,
                mapStyle = mapStyle,
                locationAccuracyLevel = locationAccuracyLevel,
                locationUpdateDelay = locationUpdateDelay,
                isShowingMapStyleDialog = false,
                isShowingLocationAccuracyLevelDialog = false,
                isShowingLocationUpdateDelayDialog = false,
                isShowingResetSettingsDialog = false
            )
        }
    }

    fun updateSettingsState(transform: (SettingsState) -> SettingsState) {
        _settingsState.value = transform(_settingsState.value)
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

    fun setIsUsingCrosshairs(enabled: Boolean) {
        viewModelScope.launch {
            updateMockControlState { it.copy(isUsingCrosshairs = enabled) }
        }
    }

    fun setBuildRouteOnRoads(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setBuildRouteOnRoads(enabled)
        }
    }

    fun setMapStyle(mapStyle: MapStyle?) {
        viewModelScope.launch {
            settingsManager.setMapStyle(mapStyle)
        }
    }

    fun setLocationAccuracyLevel(locationAccuracyLevel: LocationAccuracyLevel) {
        viewModelScope.launch {
            settingsManager.setLocationAccuracyLevel(locationAccuracyLevel)
        }
    }

    fun setIsCameraFollowingMockedLocation(value: Boolean) {
        viewModelScope.launch {
            settingsManager.setIsCameraFollowingMockedLocation(value)
        }
    }

    fun setIsCameraCurrentlyFollowingMockedLocation(value: Boolean) {
        viewModelScope.launch {
            settingsManager.setIsCameraCurrentlyFollowingMockedLocation(value)
        }
    }

    fun setIsGoingToWaitAtRouteFinish(value: Boolean) {
        viewModelScope.launch {
            settingsManager.setIsGoingToWaitAtRouteFinish(value)
        }
    }

    fun setLocationUpdateDelay(value: Float) {
        viewModelScope.launch {
            settingsManager.setLocationUpdateDelay(value)
        }
    }
}
