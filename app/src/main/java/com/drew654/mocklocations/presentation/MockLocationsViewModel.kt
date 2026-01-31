package com.drew654.mocklocations.presentation

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.SavedCameraPosition
import com.drew654.mocklocations.service.MockLocationService
import com.drew654.mocklocations.service.MockLocationService.Companion.ACTION_START_MOCKING
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MockLocationsViewModel(application: Application) : AndroidViewModel(application) {
    private val _isShowingPermissionsDialog = MutableStateFlow(false)
    val isShowingPermissionsDialog = _isShowingPermissionsDialog.asStateFlow()
    private val _cameraPosition = MutableStateFlow<SavedCameraPosition?>(null)
    val cameraPosition = _cameraPosition.asStateFlow()
    private val _hasCenteredOnUser = MutableStateFlow(false)
    val hasCenteredOnUser = _hasCenteredOnUser.asStateFlow()
    private val _controlsAreExpanded = MutableStateFlow(false)
    val controlsAreExpanded: StateFlow<Boolean> = _controlsAreExpanded.asStateFlow()
    val isMocking: StateFlow<Boolean> = MockLocationService.isMocking
    val isPaused: StateFlow<Boolean> = MockLocationService.isPaused
    private val settingsManager = SettingsManager(application)
    private val _speedMetersPerSec = MutableStateFlow(30.0)
    val speedMetersPerSec: StateFlow<Double> = _speedMetersPerSec.asStateFlow()
    val activeLocationTarget =
        settingsManager.activeLocationTargetFlow
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                LocationTarget.Empty
            )

    init {
        viewModelScope.launch {
            settingsManager.speedMetersPerSecFlow.collect {
                _speedMetersPerSec.value = it
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

    fun setIsShowingPermissionsDialog(shouldShow: Boolean) {
        _isShowingPermissionsDialog.value = shouldShow
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
        val intent = Intent(getApplication(), MockLocationService::class.java).apply {
            action = MockLocationService.ACTION_TOGGLE_PAUSE
        }
        getApplication<Application>().startService(intent)
    }

    fun pushPoint(point: LatLng) {
        viewModelScope.launch {
            val current = activeLocationTarget.value
            val updated = LocationTarget.create(current.points + point)
            settingsManager.setActiveLocationTarget(updated)
        }
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

    fun setSpeedMetersPerSec(speed: Double) {
        _speedMetersPerSec.value = speed
    }

    fun startMockLocation(context: Context) {
        if (hasFineLocationPermission(context)) {
            val target = activeLocationTarget.value
            if (target is LocationTarget.Empty) return

            viewModelScope.launch {
                settingsManager.setActiveLocationTarget(target)

                val intent = Intent(getApplication(), MockLocationService::class.java).apply {
                    action = ACTION_START_MOCKING
                }
                context.startForegroundService(intent)
            }
        } else {
            _isShowingPermissionsDialog.value = true
        }
    }

    fun stopMockLocation() {
        val intent = Intent(getApplication(), MockLocationService::class.java).apply {
            action = MockLocationService.ACTION_STOP_MOCKING
        }
        getApplication<Application>().startService(intent)

        if (clearRouteOnStop.value) {
            clearLocationTarget()
        }
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

    fun saveSpeedMetersPerSec(speed: Double) {
        viewModelScope.launch {
            settingsManager.setSpeedMetersPerSec(speed)
        }
    }

    val useCrosshairs = settingsManager.useCrosshairsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun setUseCrosshairs(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setUseCrosshairs(enabled)
        }
    }
}
