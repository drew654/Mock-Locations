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
import com.drew654.mocklocations.data.repository.ExportRepository
import com.drew654.mocklocations.data.repository.RouteRepository
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.ExpandedControlsConfigurationState
import com.drew654.mocklocations.domain.model.ExpandedControlsState
import com.drew654.mocklocations.domain.model.ExportSettingsState
import com.drew654.mocklocations.domain.model.ImportRouteOption
import com.drew654.mocklocations.domain.model.LocationAccuracyLevel
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MapStyle
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.MockLocationsUiState
import com.drew654.mocklocations.domain.model.RoutePoint
import com.drew654.mocklocations.domain.model.RouteSegment
import com.drew654.mocklocations.domain.model.SavedCameraPosition
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import com.drew654.mocklocations.service.MockLocationService
import com.drew654.mocklocations.service.MockLocationService.Companion.ACTION_RESTORE_STRAIGHT_LINE_MOCKING
import com.drew654.mocklocations.service.MockLocationService.Companion.ACTION_START_MOCKING
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MockLocationsViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(MockLocationsUiState())
    val uiState: StateFlow<MockLocationsUiState> = _uiState
    private val settingsManager = SettingsManager(application)
    val exportRepository = ExportRepository(settingsManager)
    val routeRepository = RouteRepository()
    val mockControlState = settingsManager.mockControlStateFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking { settingsManager.mockControlStateFlow.first() }
    )
    val isBuildRoutesOnRoad: StateFlow<Boolean> = settingsManager.buildRouteOnRoadsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking { settingsManager.buildRouteOnRoadsFlow.first() }
    )
    val mapStyle: StateFlow<MapStyle?> = settingsManager.mapStyleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking { settingsManager.mapStyleFlow.first() }
    )
    val locationAccuracyLevel: StateFlow<LocationAccuracyLevel> = settingsManager.locationAccuracyLevelFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking { settingsManager.locationAccuracyLevelFlow.first() }
    )
    val isCameraFollowingMockedLocation: StateFlow<Boolean> =
        settingsManager.isCameraFollowingMockedLocation.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking { settingsManager.isCameraFollowingMockedLocation.first() }
        )
    val isGoingToWaitAtRouteFinish: StateFlow<Boolean> = settingsManager.isGoingToWaitAtRouteFinishFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking { settingsManager.isGoingToWaitAtRouteFinishFlow.first() }
    )
    val isCameraCurrentlyFollowingMockedLocation: StateFlow<Boolean> =
        settingsManager.isCameraCurrentlyFollowingMockedLocationFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking { settingsManager.isCameraCurrentlyFollowingMockedLocationFlow.first() }
        )
    val currentMockedLocation: StateFlow<RoutePoint?> =
        settingsManager.currentMockedLocationFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking { settingsManager.currentMockedLocationFlow.first() }
        )
    val locationUpdateDelay: StateFlow<Float> = settingsManager.locationUpdateDelayFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking { settingsManager.locationUpdateDelayFlow.first() }
    )

    private val _expandedControlsConfigurationState = MutableStateFlow(ExpandedControlsConfigurationState())
    val expandedControlsConfigurationState: StateFlow<ExpandedControlsConfigurationState> = _expandedControlsConfigurationState.asStateFlow()
    private val _exportSettingsState = MutableStateFlow(ExportSettingsState())
    val exportSettingsState: StateFlow<ExportSettingsState> = _exportSettingsState.asStateFlow()

    init {
        viewModelScope.launch {
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
            _expandedControlsConfigurationState.value = getExpandedControlsConfigurationState()
            settingsManager.setMockControlState(mockControlState.value.copy(isWaitingForRouteFetch = false))
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
                    updateMockControlState {
                        it.copy(
                            isMocking = false,
                            isPaused = false,
                            isWaitingAtEndOfRoute = false,
                            activeLocationTarget = if (clearRouteOnStop.value) LocationTarget.Empty else (it.activeLocationTarget)
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

    fun importDataFromUri(importSettings: Boolean, importRouteOption: ImportRouteOption?) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            try {
                val json = context.contentResolver
                    .openInputStream(_uiState.value.importUri!!)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: throw IllegalStateException("Unable to read file")

                exportRepository.importFromJson(json, importSettings, importRouteOption)
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
                    .openInputStream(_uiState.value.importUri!!)
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
                    .openInputStream(_uiState.value.importUri!!)
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
                    .openInputStream(_uiState.value.importUri!!)
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
        }
    }

    fun updateCameraPosition(position: CameraPosition) {
        updateUiState {
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
        updateUiState { it.copy(isMapCenteredAfterLaunch = true) }
    }

    fun updateUiState(transform: (MockLocationsUiState) -> MockLocationsUiState) {
        val currentState = _uiState.value
        val newState = transform(currentState)
        _uiState.value = newState
    }

    fun updateExpandedControlsState(transform: (ExpandedControlsState) -> ExpandedControlsState) {
        val currentState = _uiState.value.expandedControlsState
        val newState = transform(currentState)
        updateUiState { it.copy(expandedControlsState = newState) }
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
        if (isBuildRoutesOnRoad.value) {
            if (mockControlState.value.activeLocationTarget is LocationTarget.Empty) {
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
                    start = mockControlState.value.activeLocationTarget.getLastPoint()!!,
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
        updateUiState { it.copy(importUri = uri) }
    }

    fun setShouldFocusSearchBar(value: Boolean) {
        updateUiState { it.copy(shouldFocusSearchBar = value) }
    }

    fun setSpeedUnitValue(newSpeedUnitValue: SpeedUnitValue) {
        updateExpandedControlsState { it.copy(speedUnitValue = newSpeedUnitValue) }
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
            updateMockControlState {
                it.copy(
                    isMocking = false,
                    isPaused = false,
                    isWaitingAtEndOfRoute = false,
                    activeLocationTarget = if (clearRouteOnStop.value) LocationTarget.Empty else (it.activeLocationTarget)
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

    val clearRouteOnStop = settingsManager.clearRouteOnStopFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking { settingsManager.clearRouteOnStopFlow.first() }
    )

    fun setClearRouteOnStop(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setClearRouteOnStop(enabled)
        }
    }

    val savedRoutes = settingsManager.savedRoutesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking { settingsManager.savedRoutesFlow.first() }
    )

    fun saveCurrentRoute(name: String) {
        val current = mockControlState.value.activeLocationTarget
        if (current.routeSegments.isNotEmpty()) {
            val routeToSave = LocationTarget.SavedRoute(name = name, routeSegments = current.routeSegments)
            viewModelScope.launch {
                settingsManager.saveRoute(route = routeToSave)
            }
        }
    }

    suspend fun getExpandedControlsConfigurationState(): ExpandedControlsConfigurationState {
        val speedUnitValue = settingsManager.speedUnitValueFlow.first()
        val speedSliderLowerEnd = settingsManager.speedSliderLowerEndFlow.first()
        val speedSliderUpperEnd = settingsManager.speedSliderUpperEndFlow.first()
        return ExpandedControlsConfigurationState(
            isShowingDialog = false,
            speedUnitValue = speedUnitValue,
            speedSliderLowerEnd = speedSliderLowerEnd.toString(),
            speedSliderUpperEnd = speedSliderUpperEnd.toString()
        )
    }

    fun updateExpandedControlsConfigurationState(transform: (ExpandedControlsConfigurationState) -> ExpandedControlsConfigurationState) {
        _expandedControlsConfigurationState.value = transform(_expandedControlsConfigurationState.value)
    }

    fun refreshExpandedControlsConfigurationState() {
        val currentExpandedControlsState = _uiState.value.expandedControlsState
        _expandedControlsConfigurationState.value = ExpandedControlsConfigurationState(
            isShowingDialog = false,
            speedUnitValue = currentExpandedControlsState.speedUnitValue,
            speedSliderLowerEnd = currentExpandedControlsState.speedSliderLowerEnd.toString(),
            speedSliderUpperEnd = currentExpandedControlsState.speedSliderUpperEnd.toString()
        )
    }

    fun saveExpandedControlsConfigurationState() {
        val configState = expandedControlsConfigurationState.value
        val speedSliderLowerEnd = configState.speedSliderLowerEnd.toIntOrNull() ?: 0
        val speedSliderUpperEnd = configState.speedSliderUpperEnd.toIntOrNull() ?: 100
        var speedUnitValue = configState.speedUnitValue

        if (speedUnitValue.value < speedSliderLowerEnd) {
            speedUnitValue = speedUnitValue.copy(value = speedSliderLowerEnd.toDouble())
        } else if (speedUnitValue.value > speedSliderUpperEnd) {
            speedUnitValue = speedUnitValue.copy(value = speedSliderUpperEnd.toDouble())
        }

        setSpeedUnitValue(speedUnitValue)
        saveSpeedUnitValue(speedUnitValue)

        setSpeedSliderLowerEnd(speedSliderLowerEnd)
        saveSpeedSliderLowerEnd(speedSliderLowerEnd)
        setSpeedSliderUpperEnd(speedSliderUpperEnd)
        saveSpeedSliderUpperEnd(speedSliderUpperEnd)

        updateExpandedControlsConfigurationState {
            it.copy(speedUnitValue = speedUnitValue)
        }
    }

    fun refreshExportSettingsState() {
        val currentRoutesCount = savedRoutes.value.size
        _exportSettingsState.value = ExportSettingsState(
            routesToExport = currentRoutesCount,
            isExportSettings = true,
            isExportRoutes = currentRoutesCount > 0
        )
    }

    fun updateExportSettingsState(transform: (ExportSettingsState) -> ExportSettingsState) {
        _exportSettingsState.value = transform(_exportSettingsState.value)
    }

    fun loadSavedRoute(route: LocationTarget.SavedRoute) {
        viewModelScope.launch {
            updateMockControlState { it.copy(activeLocationTarget = route) }
        }
    }

    fun deleteSavedRoute(route: LocationTarget.SavedRoute) {
        viewModelScope.launch {
            settingsManager.deleteRoute(route)
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

    fun setSpeedSliderLowerEnd(value: Int) {
        updateExpandedControlsState { it.copy(speedSliderLowerEnd = value) }
    }

    fun saveSpeedSliderLowerEnd(value: Int) {
        viewModelScope.launch {
            settingsManager.setSpeedSliderLowerEnd(value)
        }
    }

    fun setSpeedSliderUpperEnd(value: Int) {
        updateExpandedControlsState { it.copy(speedSliderUpperEnd = value) }
    }

    fun saveSpeedSliderUpperEnd(value: Int) {
        viewModelScope.launch {
            settingsManager.setSpeedSliderUpperEnd(value)
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
