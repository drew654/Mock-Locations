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
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MockLocationsViewModel(application: Application) : AndroidViewModel(application) {
    private var mockJob: Job? = null
    private val _isMocking = MutableStateFlow(false)
    val isMocking: StateFlow<Boolean> = _isMocking.asStateFlow()
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()
    private val _points = MutableStateFlow<List<LatLng>>(emptyList())
    val points: StateFlow<List<LatLng>> = _points.asStateFlow()
    val _speedMetersPerSec = MutableStateFlow(30.0)
    val speedMetersPerSec: StateFlow<Double> = _speedMetersPerSec.asStateFlow()

    private val locationManager =
        application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val providerName = LocationManager.GPS_PROVIDER

    fun togglePause() {
        _isPaused.value = !_isPaused.value
    }

    fun pushPoint(point: LatLng) {
        _points.value = _points.value + point
    }

    fun popPoint() {
        _points.value = _points.value.dropLast(1)
    }

    fun clearPoints() {
        _points.value = emptyList()
    }

    fun setSpeedMetersPerSec(speed: Double) {
        _speedMetersPerSec.value = speed
    }

    fun startMockLocation() {
        if (_points.value.isEmpty()) {
            Toast.makeText(getApplication(), "Please a point first", Toast.LENGTH_SHORT)
                .show()
        } else if (_points.value.size == 1) {
            mockLocationSinglePoint(_points.value.first())
        } else {
            mockLocationStraightLineRoute(_points.value)
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
                Toast.makeText(
                    getApplication(),
                    "Permission denied. Enable 'Select mock location app' in Developer Options.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Toast.makeText(getApplication(), "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun mockLocationStraightLineRoute(points: List<LatLng>) {
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

                for (i in 0 until points.size - 1) {
                    val start = points[i]
                    val end = points[i + 1]

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

                        val stepDistance = _speedMetersPerSec.value * (updateIntervalMs / 1000.0)

                        val fraction = currentDistance / totalDistance

                        val nextLat = start.latitude + (end.latitude - start.latitude) * fraction
                        val nextLng = start.longitude + (end.longitude - start.longitude) * fraction

                        val location = Location(providerName).apply {
                            latitude = nextLat
                            longitude = nextLng
                            altitude = 3.0
                            time = System.currentTimeMillis()
                            speed = _speedMetersPerSec.value.toFloat()
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
                Toast.makeText(getApplication(), "Permission denied.", Toast.LENGTH_SHORT).show()
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

    private val settingsManager = SettingsManager(application)

    val clearPointsOnStop = settingsManager.clearPointsOnStopFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun setClearPointsOnStop(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setClearPointsOnStop(enabled)
        }
    }
}
