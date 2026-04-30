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
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.Permission
import com.drew654.mocklocations.domain.model.RoutePoint
import com.drew654.mocklocations.domain.model.isGranted
import com.drew654.mocklocations.domain.model.isPauseVisible
import com.drew654.mocklocations.domain.model.isResumeVisible
import com.drew654.mocklocations.domain.model.toMetersPerSecond
import com.drew654.mocklocations.presentation.toLatLng
import com.drew654.mocklocations.presentation.toRoutePoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class MockLocationService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val settingsManager by lazy { SettingsManager(applicationContext) }
    private var mockJob: Job? = null
    private val locationManager by lazy { getSystemService(LOCATION_SERVICE) as LocationManager }
    private val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.FUSED_PROVIDER)
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mockControlState: StateFlow<MockControlState>
    private lateinit var isClearRouteOnStopState: StateFlow<Boolean>
    private lateinit var accuracyMetersState: StateFlow<Float>
    private lateinit var locationUpdateDelayState: StateFlow<Long>
    private var lastBroadcastLocation: Location? = null
    private var noiseLat = 0.0
    private var noiseLng = 0.0

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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

        accuracyMetersState = settingsManager.locationAccuracyLevelFlow
            .map {
                if (it.meters == 0f) {
                    noiseLat = 0.0
                    noiseLng = 0.0
                }
                it.meters
            }
            .stateIn(
                scope = serviceScope,
                started = SharingStarted.Eagerly,
                initialValue = 0f
            )

        locationUpdateDelayState = settingsManager.locationUpdateDelayFlow.map { (it * 1000).toLong() }.stateIn(
            scope = serviceScope,
            started = SharingStarted.Eagerly,
            initialValue = 1000
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
                            isWaitingAtEndOfRoute = false,
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
                    val restoreMockingPoint = withTimeoutOrNull(3000) {
                        settingsManager.currentMockedLocationFlow
                            .filterNotNull()
                            .first()
                    }
                    if (locationTarget.isRoute()) {
                        if (restoreMockingPoint == null) {
                            mockLocationStraightLineRoute(locationTarget)
                        } else {
                            restoreMockLocationStraightLineRoute(locationTarget, restoreMockingPoint)
                        }
                    } else {
                        stopMocking()
                    }
                }
            }
        }

        return START_STICKY
    }

    private fun updateNotification(mockControlState: MockControlState) {
        val hasLocationPermission = Permission.FineLocation.isGranted(application)
        if (!hasLocationPermission) return

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

    private fun updateNoiseSmooth() {
        val currentAccuracyMeters = accuracyMetersState.value
        val earthRadius = 6371000.0

        val noiseMeters = currentAccuracyMeters * (0.3 + Random.nextDouble() * 0.4)
        val randomDistance = Math.random() * noiseMeters
        val randomAngle = Math.random() * 2 * Math.PI

        val dLat = (randomDistance * cos(randomAngle)) / earthRadius
        val dLng = (randomDistance * sin(randomAngle)) / earthRadius

        val randomLat = Math.toDegrees(dLat)
        val randomLng = Math.toDegrees(dLng)

        val alpha = (currentAccuracyMeters / 50f).coerceIn(0.05f, 0.4f)

        noiseLat = noiseLat * (1 - alpha) + randomLat * alpha
        noiseLng = noiseLng * (1 - alpha) + randomLng * alpha
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
                    broadcastLocation(
                        latLng = point,
                        bearing = 0f,
                        speed = 0f
                    )
                    delay(locationUpdateDelayState.value)
                    updateNoiseSmooth()
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
            isStartedWaitingAtEndOfRoute = false,
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
            isStartedWaitingAtEndOfRoute = mockControlState.value.isWaitingAtEndOfRoute,
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
        isStartedWaitingAtEndOfRoute: Boolean,
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

                var currentSpeedMetersPerSec = settingsManager.speedUnitValueFlow.first().toMetersPerSecond()
                launch {
                    settingsManager.speedUnitValueFlow.collect { currentSpeedMetersPerSec = it.toMetersPerSecond() }
                }

                val metersPerPoint = 1.0

                var index = startIndex
                var distanceAccumulator = 0.0

                if (isStartedPaused) {
                    val routePoint = routePoints.getOrNull(index) ?: return@launch
                    broadcastLocation(
                        latLng = routePoint.latLng,
                        bearing = routePoint.bearing,
                        speed = 0f
                    )
                    delay(locationUpdateDelayState.value)
                }

                var pausedBaseLocation: Location? = null

                if (!isStartedWaitingAtEndOfRoute) {
                    while (index < routePoints.size && isActive) {
                        if (mockControlState.value.isPaused) {
                            if (pausedBaseLocation == null) {
                                pausedBaseLocation = lastBroadcastLocation
                            }

                            if (pausedBaseLocation != null) {
                                broadcastLocation(
                                    latLng = pausedBaseLocation.toLatLng(),
                                    bearing = pausedBaseLocation.bearing,
                                    speed = 0f
                                )
                            }

                            delay(locationUpdateDelayState.value)
                            updateNoiseSmooth()
                            continue
                        } else {
                            pausedBaseLocation = null
                        }

                        val routePoint = routePoints.getOrNull(index) ?: break

                        broadcastLocation(
                            latLng = routePoint.latLng,
                            bearing = routePoint.bearing,
                            speed = currentSpeedMetersPerSec.toFloat()
                        )

                        val updateIntervalMs = locationUpdateDelayState.value
                        delay(updateIntervalMs)
                        updateNoiseSmooth()

                        distanceAccumulator += currentSpeedMetersPerSec * (updateIntervalMs / 1000.0)
                        while (distanceAccumulator >= metersPerPoint && index < routePoints.size) {
                            distanceAccumulator -= metersPerPoint
                            index++
                            if (index >= routePoints.size - 1) {
                                index = routePoints.size - 1

                                val finalPoint = routePoints[index]
                                broadcastLocation(
                                    latLng = finalPoint.latLng,
                                    bearing = finalPoint.bearing,
                                    speed = 0f
                                )

                                delay(updateIntervalMs)
                                index = routePoints.size
                                break
                            }
                        }
                    }
                }

                if (settingsManager.isGoingToWaitAtRouteFinishFlow.first()) {
                    settingsManager.setMockControlState(settingsManager.mockControlStateFlow.first().copy(
                        isWaitingAtEndOfRoute = true
                    ))

                    while (mockControlState.value.isMocking) {
                        if (pausedBaseLocation == null) {
                            pausedBaseLocation = lastBroadcastLocation
                        }

                        if (pausedBaseLocation != null) {
                            broadcastLocation(
                                latLng = pausedBaseLocation.toLatLng(),
                                bearing = pausedBaseLocation.bearing,
                                speed = 0f
                            )
                        }

                        delay(locationUpdateDelayState.value)
                        updateNoiseSmooth()
                    }
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

    private suspend fun broadcastLocation(
        latLng: LatLng,
        bearing: Float,
        speed: Float
    ) {
        val now = System.currentTimeMillis()
        val elapsedNanos = SystemClock.elapsedRealtimeNanos()
        val currentAccuracy = accuracyMetersState.value

        val mockLocation = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = latLng.latitude + noiseLat
            longitude = latLng.longitude + noiseLng
            this.bearing = bearing
            this.speed = speed
            time = now
            elapsedRealtimeNanos = elapsedNanos
            accuracy = currentAccuracy
            altitude = 0.0
            verticalAccuracyMeters = 0f
            isMock = true
        }

        try {
            fusedLocationClient.setMockLocation(mockLocation)
        } catch (e: SecurityException) {
            Log.e("MockLocationService", "SecurityException: Cannot disable mock mode", e)
        }

        providers.forEach { name ->
            val providerLoc = Location(mockLocation).apply { provider = name }
            locationManager.setTestProviderLocation(name, providerLoc)
        }

        lastBroadcastLocation = mockLocation
        settingsManager.setCurrentMockedLocation(mockLocation.toRoutePoint())
    }

    private fun buildRoutePoints(
        points: List<LatLng>,
        stepMeters: Double = 1.0
    ): List<RoutePoint> {
        if (points.isEmpty()) return emptyList()
        val result = mutableListOf<RoutePoint>()

        for (i in 0 until points.size - 1) {
            val start = points[i]
            val end = points[i + 1]

            val results = FloatArray(3)
            Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results)

            val totalDistance = results[0]
            val bearing = results[1]

            var distance = 0.0
            while (distance < totalDistance) {
                val fraction = distance / totalDistance
                val lat = start.latitude + (end.latitude - start.latitude) * fraction
                val lng = start.longitude + (end.longitude - start.longitude) * fraction

                result += RoutePoint(latLng = LatLng(lat, lng), bearing = bearing)
                distance += stepMeters
            }
        }

        val finalPoint = points.last()
        val finalBearing = result.lastOrNull()?.bearing ?: 0.0f
        result += RoutePoint(latLng = finalPoint, bearing = finalBearing)

        return result
    }

    private fun setUpTestProvider() {
        try {
            fusedLocationClient.setMockMode(true)
                .addOnFailureListener { e ->
                    Log.e("MockLocationService", "Failed to set mock mode", e)
                }
        } catch (e: SecurityException) {
            Log.e("MockLocationService", "SecurityException: Cannot enable mock mode", e)
        }
        providers.forEach { name ->
            try {
                locationManager.removeTestProvider(name)
            } catch (_ : Exception) {
            }

            val properties = ProviderProperties.Builder()
                .setHasAltitudeSupport(true)
                .setHasSpeedSupport(true)
                .setHasBearingSupport(true)
                .setPowerUsage(ProviderProperties.POWER_USAGE_LOW)
                .setAccuracy(ProviderProperties.ACCURACY_FINE)
                .build()

            locationManager.addTestProvider(name, properties)
            locationManager.setTestProviderEnabled(name, true)
        }
    }

    private fun tearDownTestProvider() {
        try {
            fusedLocationClient.setMockMode(false)
                .addOnFailureListener { e ->
                    Log.e("MockLocationService", "Failed to set mock mode", e)
                }
        } catch (e: SecurityException) {
            Log.e("MockLocationService", "SecurityException: Cannot disable mock mode", e)
        }
        providers.forEach { name ->
            try {
                locationManager.setTestProviderEnabled(name, false)
                locationManager.removeTestProvider(name)
            } catch (_: Exception) {
            }
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
        lastBroadcastLocation = null

        if (isClearRouteOnStopState.value) {
            sendBroadcast(Intent(ACTION_ROUTE_FINISHED).setPackage(packageName))
        } else {
            settingsManager.setMockControlState(settingsManager.mockControlStateFlow.first().copy(
                isMocking = false,
                isPaused = false,
                isWaitingAtEndOfRoute = false
            ))
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
