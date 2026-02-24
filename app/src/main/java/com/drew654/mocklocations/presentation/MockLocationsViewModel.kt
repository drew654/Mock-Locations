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
    private val _speedUnitValue = MutableStateFlow(SpeedUnitValue(value = 30.0, speedUnit = SpeedUnit.MetersPerSecond))
    val speedUnitValue: StateFlow<SpeedUnitValue> = _speedUnitValue.asStateFlow()
    val activeLocationTarget =
        settingsManager.activeLocationTargetFlow
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                LocationTarget.Empty
            )
    val isMocking = settingsManager.isMockingFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    val isPaused = settingsManager.isPausedFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    val isUsingCrosshairs = settingsManager.isUsingCrosshairsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
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
    val isCameraFollowingMockedLocation: StateFlow<Boolean> = settingsManager.isCameraFollowingMockedLocation.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
    private val _isCameraCurrentlyFollowingMockedLocation = MutableStateFlow(false)
    val isCameraCurrentlyFollowingMockedLocation = _isCameraCurrentlyFollowingMockedLocation.asStateFlow()
    val currentMockedLocation: StateFlow<RoutePoint?> = settingsManager.currentMockedLocationFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        viewModelScope.launch {
            _speedUnitValue.value = settingsManager.speedUnitValueFlow.first()
        }

        viewModelScope.launch {
            val wasMocking = settingsManager.isMockingFlow.first()
            val activeLocationTarget = settingsManager.activeLocationTargetFlow.first()
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

    fun togglePause() {
        viewModelScope.launch {
            settingsManager.toggleIsPaused()
        }
    }

    suspend fun pushPoint(point: LatLng) {
        val current = activeLocationTarget.value
        val updated = LocationTarget.create(current.points + point)
        settingsManager.setActiveLocationTarget(updated)
    }

    fun popPoint() {
        viewModelScope.launch {
            val current = activeLocationTarget.value
            val updated = LocationTarget.create(current.points.dropLast(1))
            settingsManager.setActiveLocationTarget(updated)
        }
    }

    fun clearLocationTarget() {
        viewModelScope.launch {
            settingsManager.setActiveLocationTarget(LocationTarget.Empty)
        }
    }

    fun setSpeedUnitValue(speedUnitValue: SpeedUnitValue) {
        _speedUnitValue.value = speedUnitValue
    }

    fun startMockLocation(context: Context) {
        val target = activeLocationTarget.value
        if (target is LocationTarget.Empty) return

        viewModelScope.launch {
            settingsManager.setActiveLocationTarget(target)

            val intent = Intent(getApplication(), MockLocationService::class.java).apply {
                action = ACTION_START_MOCKING
            }
            context.startForegroundService(intent)
        }
    }

    fun stopMockLocation() {
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
        val current = activeLocationTarget.value
        if (current.points.isNotEmpty()) {
            val routeToSave = LocationTarget.SavedRoute(name, current.points)
            viewModelScope.launch {
                settingsManager.saveRoute(routeToSave)
            }
        }
    }

    fun loadSavedRoute(route: LocationTarget.SavedRoute) {
        viewModelScope.launch {
            settingsManager.setActiveLocationTarget(route)
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
            settingsManager.setIsUsingCrosshairs(enabled)
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
