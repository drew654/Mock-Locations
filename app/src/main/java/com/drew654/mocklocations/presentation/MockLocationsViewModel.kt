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
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MapStyle
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.RoutePoint
import com.drew654.mocklocations.domain.model.SavedCameraPosition
import com.drew654.mocklocations.domain.model.SpeedUnit
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

class MockLocationsViewModel(application: Application) : AndroidViewModel(application) {
    private val _cameraPosition = MutableStateFlow<SavedCameraPosition?>(null)
    val cameraPosition = _cameraPosition.asStateFlow()
    private val _hasCenteredOnUser = MutableStateFlow(false)
    val hasCenteredOnUser = _hasCenteredOnUser.asStateFlow()
    private val _controlsAreExpanded = MutableStateFlow(false)
    val controlsAreExpanded: StateFlow<Boolean> = _controlsAreExpanded.asStateFlow()
    private val settingsManager = SettingsManager(application)
    val repository = ExportRepository(settingsManager)
    private val _speedUnitValue =
        MutableStateFlow(SpeedUnitValue(value = 30.0, speedUnit = SpeedUnit.MilesPerHour))
    val speedUnitValue: StateFlow<SpeedUnitValue> = _speedUnitValue.asStateFlow()
    val mockControlState = settingsManager.mockControlStateFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MockControlState()
    )
    val mapStyle: StateFlow<MapStyle?> = settingsManager.mapStyleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    val speedSliderLowerEnd: StateFlow<Int> = settingsManager.speedSliderLowerEndFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    val speedSliderUpperEnd: StateFlow<Int> = settingsManager.speedSliderUpperEndFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 100
    )
    val isCameraFollowingMockedLocation: StateFlow<Boolean> =
        settingsManager.isCameraFollowingMockedLocation.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    private val _isCameraCurrentlyFollowingMockedLocation = MutableStateFlow(false)
    val isCameraCurrentlyFollowingMockedLocation =
        _isCameraCurrentlyFollowingMockedLocation.asStateFlow()
    val currentMockedLocation: StateFlow<RoutePoint?> =
        settingsManager.currentMockedLocationFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        viewModelScope.launch {
            _speedUnitValue.value = settingsManager.speedUnitValueFlow.first()
        }

        viewModelScope.launch {
            val wasMocking = settingsManager.mockControlStateFlow.first().isMocking
            val activeLocationTarget = settingsManager.mockControlStateFlow.first().activeLocationTarget
            if (wasMocking) {
                Intent(application, MockLocationService::class.java).apply {
                    action =
                        if (activeLocationTarget is LocationTarget.Route || activeLocationTarget is LocationTarget.SavedRoute) {
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
                    if (clearRouteOnStop.value) {
                        clearLocationTarget()
                    }
                }
            }
        }, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    fun exportDataToUri(uri: Uri, exportSettings: Boolean, exportRoutes: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>().applicationContext
                val jsonString = repository.generateExportToJson(context, exportSettings, exportRoutes)
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

    fun importDataFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext

            try {
                val json = context.contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: throw IllegalStateException("Unable to read file")

                repository.importFromJson(json)
                _speedUnitValue.value = settingsManager.speedUnitValueFlow.first()

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

    fun resetSettingsToDefault() {
        viewModelScope.launch {
            settingsManager.resetToDefault()
            _speedUnitValue.value = settingsManager.speedUnitValueFlow.first()
        }
    }

    fun updateCameraPosition(position: CameraPosition) {
        _cameraPosition.value = SavedCameraPosition(
            latitude = position.target.latitude,
            longitude = position.target.longitude,
            zoom = position.zoom
        )
    }

    fun markCenteredOnUser() {
        _hasCenteredOnUser.value = true
    }

    fun setControlsAreExpanded(expanded: Boolean) {
        _controlsAreExpanded.value = expanded
    }

    fun setMockControlState(mockControlState: MockControlState) {
        viewModelScope.launch {
            settingsManager.setMockControlState(mockControlState)
        }
    }

    private suspend fun updateControlState(transform: (MockControlState) -> MockControlState) {
        val currentState = settingsManager.mockControlStateFlow.first()
        val newState = transform(currentState)
        settingsManager.setMockControlState(newState)
    }

    fun togglePause() {
        viewModelScope.launch {
            updateControlState { it.copy(isPaused = !it.isPaused) }
        }
    }

    suspend fun pushPoint(point: LatLng) {
        updateControlState { it.copy(activeLocationTarget = LocationTarget.create(it.activeLocationTarget.points + point)) }
    }

    fun popPoint() {
        viewModelScope.launch {
            updateControlState { it.copy(activeLocationTarget = LocationTarget.create(it.activeLocationTarget.points.dropLast(1))) }
        }
    }

    fun clearLocationTarget() {
        viewModelScope.launch {
            updateControlState { it.copy(activeLocationTarget = LocationTarget.Empty) }
        }
    }

    fun setSpeedUnitValue(speedUnitValue: SpeedUnitValue) {
        _speedUnitValue.value = speedUnitValue
    }

    fun startMockLocation(context: Context, pushPoint: LatLng? = null) {
        viewModelScope.launch {
            updateControlState { state ->
                val target = if (pushPoint == null) {
                    state.activeLocationTarget
                } else {
                    LocationTarget.create(state.activeLocationTarget.points + pushPoint)
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
        setMockControlState(mockControlState.value.copy(
            isMocking = false,
            isPaused = false,
            activeLocationTarget = if (clearRouteOnStop.value) LocationTarget.Empty else mockControlState.value.activeLocationTarget
        ))
        val intent = Intent(getApplication(), MockLocationService::class.java).apply {
            action = MockLocationService.ACTION_STOP_MOCKING
        }
        getApplication<Application>().startService(intent)
    }

    val clearRouteOnStop = settingsManager.clearRouteOnStopFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun setClearRouteOnStop(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setClearRouteOnStop(enabled)
        }
    }

    val savedRoutes = settingsManager.savedRoutesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun saveCurrentRoute(name: String) {
        val current = mockControlState.value.activeLocationTarget
        if (current.points.isNotEmpty()) {
            val routeToSave = LocationTarget.SavedRoute(name, current.points)
            viewModelScope.launch {
                settingsManager.saveRoute(routeToSave)
            }
        }
    }

    fun loadSavedRoute(route: LocationTarget.SavedRoute) {
        viewModelScope.launch {
            updateControlState { it.copy(activeLocationTarget = route) }
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
            updateControlState { it.copy(isUsingCrosshairs = enabled) }
        }
    }

    fun setMapStyle(mapStyle: MapStyle?) {
        viewModelScope.launch {
            settingsManager.setMapStyle(mapStyle)
        }
    }

    fun setSpeedSliderLowerEnd(value: Int) {
        viewModelScope.launch {
            settingsManager.setSpeedSliderLowerEnd(value)
        }
    }

    fun setSpeedSliderUpperEnd(value: Int) {
        viewModelScope.launch {
            settingsManager.setSpeedSliderUpperEnd(value)
        }
    }

    fun setIsCameraFollowingMockedLocation(value: Boolean) {
        _isCameraCurrentlyFollowingMockedLocation.value = value
        viewModelScope.launch {
            settingsManager.setIsCameraFollowingMockedLocation(value)
        }
    }

    fun setIsCameraCurrentlyFollowingMockedLocation(value: Boolean) {
        _isCameraCurrentlyFollowingMockedLocation.value = value
    }
}
