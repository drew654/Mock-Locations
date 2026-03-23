package com.drew654.mocklocations.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.IBinder
import android.os.SystemClock
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.RoutePoint
import com.drew654.mocklocations.domain.model.isPauseVisible
import com.drew654.mocklocations.domain.model.isResumeVisible
import com.drew654.mocklocations.domain.model.toMetersPerSecond
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.getValue

class MockLocationService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val settingsManager by lazy { SettingsManager(applicationContext) }
    private var mockJob: Job? = null
    private val locationManager by lazy { getSystemService(LOCATION_SERVICE) as LocationManager }
    private val providerName = LocationManager.GPS_PROVIDER

    private lateinit var mockControlState: StateFlow<MockControlState>
    private lateinit var isClearRouteOnStopState: StateFlow<Boolean>

    companion object {
        const val CHANNEL_ID = "mock_location_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START_MOCKING = "ACTION_START_MOCKING"
        const val ACTION_STOP_MOCKING = "ACTION_STOP_MOCKING"
        const val ACTION_STOP_MOCKING_NOTIFICATION = "ACTION_STOP_MOCKING_NOTIFICATION"
        const val ACTION_PAUSE_MOCKING_NOTIFICATION = "ACTION_PAUSE_MOCKING_NOTIFICATION"
        const val ACTION_ROUTE_FINISHED = "ACTION_ROUTE_FINISHED"
        const val ACTION_RESTORE_STRAIGHT_LINE_MOCKING = "ACTION_RESTORE_STRAIGHT_LINE_MOCKING"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        mockControlState = settingsManager.mockControlStateFlow.stateIn(
            scope = serviceScope,
            started = SharingStarted.Eagerly,
            initialValue = MockControlState()
        )

        isClearRouteOnStopState = settingsManager.clearRouteOnStopFlow.stateIn(
            scope = serviceScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

        serviceScope.launch {
            mockControlState.collect { mockControlState ->
                updateNotification(mockControlState)
            }
        }
    }

    override fun onDestroy() {
        try {
            tearDownTestProvider()
        } catch (_: Exception) {
        }

        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updateNotification(mockControlState.value)

        when (intent?.action) {
            ACTION_START_MOCKING -> {
                serviceScope.launch {
                    val locationTarget = settingsManager.mockControlStateFlow.first().activeLocationTarget
                    when (locationTarget) {
                        is LocationTarget.Empty -> stopMocking()
                        is LocationTarget.SinglePoint -> mockLocationSinglePoint(locationTarget.point)
                        else -> mockLocationStraightLineRoute(locationTarget)
                    }
                }
            }

            ACTION_STOP_MOCKING -> {
                serviceScope.launch {
                    stopMocking()
                }
            }

            ACTION_STOP_MOCKING_NOTIFICATION -> {
                serviceScope.launch {
                    settingsManager.setMockControlState(
                        settingsManager.mockControlStateFlow.first().copy(
                            isMocking = false,
                            isPaused = false,
                            activeLocationTarget = if (isClearRouteOnStopState.value) LocationTarget.Empty else settingsManager.mockControlStateFlow.first().activeLocationTarget
                        )
                    )

                    stopMocking()
                }
            }

            ACTION_PAUSE_MOCKING_NOTIFICATION -> {
                serviceScope.launch {
                    val current = settingsManager.mockControlStateFlow.first().isPaused
                    settingsManager.setMockControlState(settingsManager.mockControlStateFlow.first().copy(isPaused = !current))
                }
            }

            ACTION_RESTORE_STRAIGHT_LINE_MOCKING -> {
                serviceScope.launch {
                    val locationTarget = settingsManager.mockControlStateFlow.first().activeLocationTarget
                    val restoreMockingPoint = settingsManager.currentMockedLocationFlow.first()
                    if (locationTarget is LocationTarget.Route || locationTarget is LocationTarget.SavedRoute) {
                        restoreMockLocationStraightLineRoute(locationTarget, restoreMockingPoint!!)
                    } else {
                        stopMocking()
                    }
                }
            }
        }

        return START_STICKY
    }

    private fun updateNotification(mockControlState: MockControlState) {
        val stopMockingIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, MockLocationService::class.java).apply {
                action = ACTION_STOP_MOCKING_NOTIFICATION
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseMockingIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, MockLocationService::class.java).apply {
                action = ACTION_PAUSE_MOCKING_NOTIFICATION
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Mocking Active")
            .setContentText("Your location is currently being mocked.")
            .setSmallIcon(R.drawable.baseline_my_location_24)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(
                R.drawable.baseline_stop_24,
                "Stop",
                stopMockingIntent
            )
            .apply {
                if (mockControlState.isPauseVisible()) {
                    addAction(
                        R.drawable.baseline_pause_24,
                        "Pause",
                        pauseMockingIntent
                    )
                } else if (mockControlState.isResumeVisible()) {
                    addAction(
                        R.drawable.baseline_play_arrow_24,
                        "Resume",
                        pauseMockingIntent
                    )
                }
            }
            .build()

        startForeground(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )
    }

    private fun mockLocationSinglePoint(point: LatLng) {
        mockJob?.cancel()

        mockJob = serviceScope.launch {
            try {
                setUpTestProvider()

                Toast.makeText(
                    this@MockLocationService,
                    "Location Mocking Started",
                    Toast.LENGTH_SHORT
                ).show()

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
                stopMockingInternal()
            }
        }
    }

    private fun mockLocationStraightLineRoute(locationTarget: LocationTarget) {
        val routePoints = buildRoutePoints(locationTarget.points)

        startRouteMocking(
            routePoints = routePoints,
            startIndex = 0,
            isStartedPaused = false,
            startedMessage = "Route Mocking Started"
        )
    }

    private fun restoreMockLocationStraightLineRoute(
        locationTarget: LocationTarget,
        restorePoint: RoutePoint
    ) {
        val routePoints = buildRoutePoints(locationTarget.points)
        val startIndex = routePoints.closestIndexTo(restorePoint)

        startRouteMocking(
            routePoints = routePoints,
            startIndex = startIndex,
            isStartedPaused = mockControlState.value.isPaused,
            startedMessage = "Route Mocking Restored"
        )
    }

    private fun List<RoutePoint>.closestIndexTo(target: RoutePoint): Int =
        indexOfFirst {
            it.latLng == target.latLng
        }.takeIf { it != -1 }
            ?: indices.minByOrNull { i ->
                val rp = this[i]
                val results = FloatArray(1)
                Location.distanceBetween(
                    rp.latLng.latitude,
                    rp.latLng.longitude,
                    target.latLng.latitude,
                    target.latLng.longitude,
                    results
                )
                results[0]
            } ?: 0

    private fun startRouteMocking(
        routePoints: List<RoutePoint>,
        startIndex: Int,
        isStartedPaused: Boolean,
        startedMessage: String
    ) {
        mockJob?.cancel()

        mockJob = serviceScope.launch {
            try {
                setUpTestProvider()

                Toast.makeText(
                    this@MockLocationService,
                    startedMessage,
                    Toast.LENGTH_SHORT
                ).show()

                var currentSpeedMetersPerSec = 30.0
                launch {
                    settingsManager.speedUnitValueFlow.collect { currentSpeedMetersPerSec = it.speedUnit.toMetersPerSecond(it.value) }
                }

                val updateIntervalMs = 1000L
                val metersPerPoint = 1.0

                var index = startIndex
                var distanceAccumulator = 0.0
                var lastBroadcastLocation: Location? = null

                if (isStartedPaused) {
                    val routePoint = routePoints.getOrNull(index) ?: return@launch

                    val location = Location(providerName).apply {
                        latitude = routePoint.latLng.latitude
                        longitude = routePoint.latLng.longitude
                        bearing = routePoint.bearing
                        speed = currentSpeedMetersPerSec.toFloat()
                        time = System.currentTimeMillis()
                        elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                        accuracy = 3f
                    }

                    lastBroadcastLocation = location
                    locationManager.setTestProviderLocation(providerName, location)
                    settingsManager.setCurrentMockedLocation(routePoint)

                    delay(updateIntervalMs)
                }

                while (index < routePoints.size && isActive) {
                    if (mockControlState.value.isPaused) {
                        lastBroadcastLocation?.let { loc ->
                            loc.time = System.currentTimeMillis()
                            loc.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                            locationManager.setTestProviderLocation(providerName, loc)
                            settingsManager.setCurrentMockedLocation(
                                RoutePoint(
                                    LatLng(loc.latitude, loc.longitude),
                                    loc.bearing
                                )
                            )
                        }
                        delay(updateIntervalMs)
                        continue
                    }

                    distanceAccumulator += currentSpeedMetersPerSec * (updateIntervalMs / 1000.0)

                    while (distanceAccumulator >= metersPerPoint && index < routePoints.size) {
                        distanceAccumulator -= metersPerPoint
                        index++
                    }

                    val routePoint = routePoints.getOrNull(index) ?: break

                    val location = Location(providerName).apply {
                        latitude = routePoint.latLng.latitude
                        longitude = routePoint.latLng.longitude
                        bearing = routePoint.bearing
                        speed = currentSpeedMetersPerSec.toFloat()
                        time = System.currentTimeMillis()
                        elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                        accuracy = 3f
                    }

                    lastBroadcastLocation = location
                    locationManager.setTestProviderLocation(providerName, location)
                    settingsManager.setCurrentMockedLocation(routePoint)

                    delay(updateIntervalMs)
                }

                Toast.makeText(
                    this@MockLocationService,
                    "Route Finished",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                handleError(e)
            } finally {
                stopMockingInternal()
            }
        }
    }

    private fun buildRoutePoints(
        points: List<LatLng>,
        stepMeters: Double = 1.0
    ): List<RoutePoint> {
        val result = mutableListOf<RoutePoint>()

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

            var distance = 0.0
            while (distance <= totalDistance) {
                val fraction = distance / totalDistance

                val lat = start.latitude + (end.latitude - start.latitude) * fraction
                val lng = start.longitude + (end.longitude - start.longitude) * fraction

                result += RoutePoint(
                    latLng = LatLng(lat, lng),
                    bearing = bearing
                )

                distance += stepMeters
            }
        }

        return result
    }

    private fun setUpTestProvider() {
        try {
            locationManager.removeTestProvider(providerName)
        } catch (_: Exception) {
        }

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
        } catch (_: Exception) {
        }
    }

    private fun handleError(e: Exception) {
        if (e !is kotlinx.coroutines.CancellationException) {
            serviceScope.launch(Dispatchers.Main) {
                Toast.makeText(
                    this@MockLocationService,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private suspend fun stopMockingInternal() {
        tearDownTestProvider()
        settingsManager.setCurrentMockedLocation(null)

        if (isClearRouteOnStopState.value) {
            sendBroadcast(Intent(ACTION_ROUTE_FINISHED).setPackage(packageName))
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private suspend fun stopMocking() {
        mockJob?.cancelAndJoin()
        mockJob = null
        stopMockingInternal()
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
