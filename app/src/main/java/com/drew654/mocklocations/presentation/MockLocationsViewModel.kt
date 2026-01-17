package com.drew654.mocklocations.presentation

import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.SystemClock
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.LocationTarget
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MockLocationsViewModel(application: Application) : AndroidViewModel(application) {
    private var mockJob: Job? = null
    private val _isShowingPermissionsDialog = MutableStateFlow(false)
    val isShowingPermissionsDialog = _isShowingPermissionsDialog.asStateFlow()
    private val _isMocking = MutableStateFlow(false)
    val isMocking: StateFlow<Boolean> = _isMocking.asStateFlow()
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()
    private val _locationTarget = MutableStateFlow<LocationTarget>(LocationTarget.Empty)
    val locationTarget: StateFlow<LocationTarget> = _locationTarget.asStateFlow()
    private val locationManager =
        application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val providerName = LocationManager.GPS_PROVIDER
    private val settingsManager = SettingsManager(application)
    private val _speedMetersPerSec = MutableStateFlow(30.0)
    val speedMetersPerSec: StateFlow<Double> = _speedMetersPerSec.asStateFlow()

    init {
        viewModelScope.launch {
            settingsManager.speedMetersPerSecFlow.collect {
                _speedMetersPerSec.value = it
            }
        }
    }

    fun setIsShowingPermissionsDialog(shouldShow: Boolean) {
        _isShowingPermissionsDialog.value = shouldShow
    }

    fun togglePause() {
        _isPaused.value = !_isPaused.value
    }

    fun pushPoint(point: LatLng) {
        _locationTarget.update { current ->
            LocationTarget.create(current.points + point)
        }
    }

    fun popPoint() {
        _locationTarget.update { current ->
            LocationTarget.create(current.points.dropLast(1))
        }
    }

    fun clearLocationTarget() {
        _locationTarget.value = LocationTarget.Empty
    }

    fun setSpeedMetersPerSec(speed: Double) {
        _speedMetersPerSec.value = speed
    }

    fun startMockLocation(context: Context) {
        if (hasFineLocationPermission(context)) {
            when (_locationTarget.value) {
                is LocationTarget.Empty -> {
                    Toast.makeText(
                        getApplication(),
                        "Please place a point first",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is LocationTarget.SinglePoint -> {
                    mockLocationSinglePoint(_locationTarget.value.points.first())
                }

                else -> {
                    mockLocationStraightLineRoute(_locationTarget.value)
                }
            }
        } else {
            _isShowingPermissionsDialog.value = true
        }
    }

    private fun mockLocationSinglePoint(point: LatLng) {
        mockJob?.cancel()

        mockJob = viewModelScope.launch {
            try {
                try {
                    locationManager.removeTestProvider(providerName)
                } catch (e: Exception) {
                }

                locationManager.addTestProvider(
                    providerName,
                    false,
                    false,
                    false,
                    false,
                    true,
                    true,
                    true,
                    ProviderProperties.POWER_USAGE_LOW,
                    ProviderProperties.ACCURACY_FINE
                )

                locationManager.setTestProviderEnabled(providerName, true)

                _isMocking.value = true
                Toast.makeText(getApplication(), "Location Mocking Started", Toast.LENGTH_SHORT)
                    .show()

                while (true) {
                    val location = Location(providerName).apply {
                        latitude = point.latitude
                        longitude = point.longitude
                        altitude = 3.0
                        time = System.currentTimeMillis()
                        speed = 0.01f
                        bearing = 0.0f
                        accuracy = 3.0f
                        elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                        bearingAccuracyDegrees = 0.1f
                        verticalAccuracyMeters = 0.1f
                        speedAccuracyMetersPerSecond = 0.01f
                    }

                    locationManager.setTestProviderLocation(providerName, location)

                    delay(1000)
                }
            } catch (e: SecurityException) {
                _isShowingPermissionsDialog.value = true
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Toast.makeText(getApplication(), "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun mockLocationStraightLineRoute(locationTarget: LocationTarget) {
        mockJob?.cancel()
        _isPaused.value = false

        mockJob = viewModelScope.launch {
            try {
                try {
                    locationManager.removeTestProvider(providerName)
                } catch (e: Exception) {
                }
                locationManager.addTestProvider(
                    providerName,
                    false,
                    false,
                    false,
                    false,
                    true,
                    true,
                    true,
                    ProviderProperties.POWER_USAGE_LOW,
                    ProviderProperties.ACCURACY_FINE
                )
                locationManager.setTestProviderEnabled(providerName, true)

                _isMocking.value = true
                Toast.makeText(getApplication(), "Route Mocking Started", Toast.LENGTH_SHORT).show()

                val updateIntervalMs = 1000L
                var lastBroadcastLocation: Location? = null

                for (i in 0 until locationTarget.points.size - 1) {
                    val start = locationTarget.points[i]
                    val end = locationTarget.points[i + 1]

                    val results = FloatArray(3)
                    Location.distanceBetween(
                        start.latitude, start.longitude,
                        end.latitude, end.longitude,
                        results
                    )
                    val totalDistance = results[0]
                    val bearing = results[1]

                    var currentDistance = 0.0

                    while (currentDistance < totalDistance) {
                        if (mockJob?.isActive == false) return@launch

                        if (_isPaused.value) {
                            lastBroadcastLocation?.let { loc ->
                                loc.time = System.currentTimeMillis()
                                loc.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                                locationManager.setTestProviderLocation(providerName, loc)
                            }
                            delay(updateIntervalMs)
                            continue
                        }

                        val stepDistance = speedMetersPerSec.value * (updateIntervalMs / 1000.0)

                        val fraction = currentDistance / totalDistance

                        val nextLat = start.latitude + (end.latitude - start.latitude) * fraction
                        val nextLng = start.longitude + (end.longitude - start.longitude) * fraction

                        val location = Location(providerName).apply {
                            latitude = nextLat
                            longitude = nextLng
                            altitude = 3.0
                            time = System.currentTimeMillis()
                            speed = speedMetersPerSec.value.toFloat()
                            this.bearing = bearing
                            accuracy = 3.0f
                            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                            bearingAccuracyDegrees = 0.1f
                            verticalAccuracyMeters = 0.1f
                            speedAccuracyMetersPerSecond = 0.01f
                        }

                        lastBroadcastLocation = location
                        locationManager.setTestProviderLocation(providerName, location)

                        delay(updateIntervalMs)
                        currentDistance += stepDistance
                    }
                }

                Toast.makeText(getApplication(), "Route Finished", Toast.LENGTH_SHORT).show()
                stopMockLocation()
            } catch (e: SecurityException) {
                _isShowingPermissionsDialog.value = true
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Toast.makeText(getApplication(), "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    fun stopMockLocation() {
        _isMocking.value = false
        _isPaused.value = false
        mockJob?.cancel()
        mockJob = null
        if (clearRouteOnStop.value) {
            clearLocationTarget()
        }

        try {
            locationManager.removeTestProvider(providerName)
            Toast.makeText(getApplication(), "Mock Location Stopped", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalArgumentException) {
            Toast.makeText(getApplication(), "Mock Location already stopped", Toast.LENGTH_SHORT)
                .show()
        } catch (e: Exception) {
            Toast.makeText(
                getApplication(),
                "Error stopping mock: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopMockLocation()
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
        val current = _locationTarget.value
        if (current.points.isNotEmpty()) {
            val routeToSave = LocationTarget.SavedRoute(name, current.points)
            viewModelScope.launch {
                settingsManager.saveRoute(routeToSave)
            }
        }
    }

    fun loadSavedRoute(route: LocationTarget.SavedRoute) {
        _locationTarget.value = route
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
}
