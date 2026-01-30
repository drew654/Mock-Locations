package com.drew654.mocklocations.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.provider.ProviderProperties
import android.os.IBinder
import android.os.SystemClock
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.LocationTarget
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MockLocationService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val settingsManager by lazy { SettingsManager(applicationContext) }
    private var mockJob: kotlinx.coroutines.Job? = null
    private val locationManager by lazy { getSystemService(LOCATION_SERVICE) as android.location.LocationManager }
    private val providerName = android.location.LocationManager.GPS_PROVIDER

    companion object {
        const val CHANNEL_ID = "mock_location_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START_MOCKING = "ACTION_START_MOCKING"
        const val ACTION_STOP_MOCKING = "ACTION_STOP_MOCKING"
        const val ACTION_TOGGLE_PAUSE = "ACTION_TOGGLE_PAUSE"
        const val ACTION_ROUTE_FINISHED = "ACTION_ROUTE_FINISHED"
        private val _isMocking = kotlinx.coroutines.flow.MutableStateFlow(false)
        val isMocking = _isMocking.asStateFlow()
        private val _isPaused = kotlinx.coroutines.flow.MutableStateFlow(false)
        val isPaused = _isPaused.asStateFlow()

    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Mocking Active")
            .setContentText("Your location is currently being spoofed.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        when (intent?.action) {
            ACTION_START_MOCKING -> {
                serviceScope.launch {
                    val locationTarget = settingsManager.activeRouteFlow.first()
                    when (locationTarget) {
                        is LocationTarget.Empty -> stopSelf()
                        is LocationTarget.SinglePoint -> mockLocationSinglePoint(locationTarget.point)
                        else -> mockLocationStraightLineRoute(locationTarget)
                    }
                }
            }

            ACTION_STOP_MOCKING -> {
                mockJob?.cancel()
                tearDownTestProvider()
                _isMocking.value = false
                stopSelf()
            }

            ACTION_TOGGLE_PAUSE -> {
                _isPaused.value = !_isPaused.value
            }
        }

        return START_STICKY
    }

    private fun mockLocationSinglePoint(point: LatLng) {
        mockJob?.cancel()

        mockJob = serviceScope.launch {
            try {
                setUpTestProvider()
                _isMocking.value = true

                Toast.makeText(this@MockLocationService, "Location Mocking Started", Toast.LENGTH_SHORT)
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
            } catch (e: Exception) {
                handleError(e)
            } finally {
                tearDownTestProvider()
                _isMocking.value = false
            }
        }
    }

    private fun mockLocationStraightLineRoute(locationTarget: LocationTarget) {
        mockJob?.cancel()
        _isPaused.value = false

        mockJob = serviceScope.launch {
            try {
                setUpTestProvider()
                _isMocking.value = true

                Toast.makeText(this@MockLocationService, "Route Mocking Started", Toast.LENGTH_SHORT).show()

                var currentSpeed = 30.0
                launch {
                    settingsManager.speedMetersPerSecFlow.collect { currentSpeed = it }
                }

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

                        val stepDistance = currentSpeed * (updateIntervalMs / 1000.0)
                        val fraction = currentDistance / totalDistance

                        val nextLat = start.latitude + (end.latitude - start.latitude) * fraction
                        val nextLng = start.longitude + (end.longitude - start.longitude) * fraction

                        val location = Location(providerName).apply {
                            latitude = nextLat
                            longitude = nextLng
                            altitude = 3.0
                            time = System.currentTimeMillis()
                            speed = currentSpeed.toFloat()
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

                Toast.makeText(this@MockLocationService, "Route Finished", Toast.LENGTH_SHORT).show()

                sendBroadcast(Intent(ACTION_ROUTE_FINISHED).setPackage(packageName))

                stopSelf()
            } catch (e: Exception) {
                handleError(e)
            } finally {
                tearDownTestProvider()
                _isMocking.value = false
            }
        }
    }

    private fun setUpTestProvider() {
        try {
            locationManager.removeTestProvider(providerName)
        } catch (e: Exception) {}

        locationManager.addTestProvider(
            providerName, false, false, false, false, true, true, true,
            ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_FINE
        )
        locationManager.setTestProviderEnabled(providerName, true)
    }

    private fun tearDownTestProvider() {
        try {
            locationManager.setTestProviderEnabled(providerName, false)
            locationManager.removeTestProvider(providerName)
        } catch (e: Exception) {
        }
    }

    private fun handleError(e: Exception) {
        if (e !is kotlinx.coroutines.CancellationException) {
            serviceScope.launch(Dispatchers.Main) {
                Toast.makeText(this@MockLocationService, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Mock Location Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Running location simulation in the background"
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
