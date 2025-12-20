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
import com.drew654.mocklocations.domain.model.Coordinates
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
    private val _coordinates = MutableStateFlow<Coordinates?>(null)
    val coordinates: StateFlow<Coordinates?> = _coordinates.asStateFlow()

    private val locationManager =
        application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val providerName = LocationManager.GPS_PROVIDER

    fun setCoordinates(coordinates: Coordinates?) {
        _coordinates.value = coordinates
    }

    fun startMockLocation() {
        if (_coordinates.value == null) {
            Toast.makeText(getApplication(), "Please set coordinates first", Toast.LENGTH_SHORT)
                .show()
        } else {
            mockLocation(_coordinates.value!!)
        }
    }

    private fun mockLocation(coordinates: Coordinates) {
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
                        latitude = coordinates.latitude
                        longitude = coordinates.longitude
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


    fun stopMockLocation() {
        _isMocking.value = false
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
