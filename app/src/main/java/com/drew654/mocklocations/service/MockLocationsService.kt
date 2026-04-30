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
import com.drew654.mocklocations.domain.model.RoutePoint
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
                        else -> mockLocationRoute(locationTarget)
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
                    val restoreMockingPoint = settingsManager.currentMockedLocationFlow.first()
                    if (locationTarget.isRoute()) {
                        restoreMockLocationRoute(locationTarget, restoreMockingPoint!!)
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

    private fun mockLocationRoute(locationTarget: LocationTarget) {
        val points = locationTarget.getAllPoints()

        startRouteMocking(
            anchorPoints = points,
            startSegmentIndex = 0,
            startDistanceInSegment = 0.0,
            isStartedWaitingAtEndOfRoute = false,
            startedMessage = "Route Mocking Started"
        )
    }

    private fun restoreMockLocationRoute(locationTarget: LocationTarget, restorePoint: RoutePoint) {
        val points = locationTarget.getAllPoints()
        if (points.size < 2) return

        val (segmentIndex, distanceInSegment) = findProgressOnRoute(points, restorePoint.latLng)

        startRouteMocking(
            anchorPoints = points,
            startSegmentIndex = segmentIndex,
            startDistanceInSegment = distanceInSegment,
            isStartedWaitingAtEndOfRoute = mockControlState.value.isWaitingAtEndOfRoute,
            startedMessage = "Route Mocking Restored"
        )
    }

    private fun interpolate(start: LatLng, end: LatLng, fraction: Double): LatLng {
        val lat = start.latitude + (end.latitude - start.latitude) * fraction
        val lng = start.longitude + (end.longitude - start.longitude) * fraction
        return LatLng(lat, lng)
    }

    private fun findProgressOnRoute(
        anchorPoints: List<LatLng>,
        restorePoint: LatLng
    ): Pair<Int, Double> {
        var closestSegmentIndex = 0
        var closestDistanceInSegment = 0.0
        var minTotalDistanceToLine = Double.MAX_VALUE

        for (i in 0 until anchorPoints.size - 1) {
            val p1 = anchorPoints[i]
            val p2 = anchorPoints[i + 1]

            val results = FloatArray(3)
            Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results)
            val segmentLength = results[0].toDouble()

            Location.distanceBetween(
                p1.latitude,
                p1.longitude,
                restorePoint.latitude,
                restorePoint.longitude,
                results
            )
            val distanceToStart = results[0].toDouble()

            Location.distanceBetween(
                p2.latitude,
                p2.longitude,
                restorePoint.latitude,
                restorePoint.longitude,
                results
            )
            val distanceToEnd = results[0].toDouble()

            val deviation = (distanceToStart + distanceToEnd) - segmentLength

            if (deviation < minTotalDistanceToLine) {
                minTotalDistanceToLine = deviation
                closestSegmentIndex = i
                closestDistanceInSegment = distanceToStart
            }
        }

        return Pair(closestSegmentIndex, closestDistanceInSegment)
    }

    private fun startRouteMocking(
        anchorPoints: List<LatLng>,
        startSegmentIndex: Int,
        startDistanceInSegment: Double,
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

                var currentSpeedMetersPerSec =
                    settingsManager.speedUnitValueFlow.first().toMetersPerSecond()
                launch {
                    settingsManager.speedUnitValueFlow.collect {
                        currentSpeedMetersPerSec = it.toMetersPerSecond()
                    }
                }

                var segmentIndex = startSegmentIndex
                var distanceInSegment = startDistanceInSegment
                var pausedBaseLocation: Location? = null

                if (!isStartedWaitingAtEndOfRoute) {
                    while (segmentIndex < anchorPoints.size - 1 && isActive) {
                        val start = anchorPoints[segmentIndex]
                        val end = anchorPoints[segmentIndex + 1]

                        val results = FloatArray(3)
                        Location.distanceBetween(
                            start.latitude,
                            start.longitude,
                            end.latitude,
                            end.longitude,
                            results
                        )
                        val segmentLength = results[0].toDouble()
                        val bearing = results[1]

                        while (distanceInSegment < segmentLength && isActive) {
                            val updateIntervalMs = locationUpdateDelayState.value

                            if (mockControlState.value.isPaused) {
                                if (pausedBaseLocation == null) {
                                    pausedBaseLocation = lastBroadcastLocation
                                }

                                pausedBaseLocation?.let { base ->
                                    broadcastLocation(base.toLatLng(), base.bearing, 0f)
                                }

                                delay(updateIntervalMs)
                                updateNoiseSmooth()
                                continue
                            } else {
                                pausedBaseLocation = null
                            }

                            val fraction = (distanceInSegment / segmentLength).coerceIn(0.0, 1.0)
                            val currentPosition = interpolate(start, end, fraction)

                            broadcastLocation(
                                currentPosition,
                                bearing,
                                currentSpeedMetersPerSec.toFloat()
                            )

                            delay(updateIntervalMs)
                            updateNoiseSmooth()

                            distanceInSegment += currentSpeedMetersPerSec * (updateIntervalMs / 1000.0)
                        }

                        if (isActive) {
                            distanceInSegment -= segmentLength
                            segmentIndex++
                        }
                    }
                }

                if (settingsManager.isGoingToWaitAtRouteFinishFlow.first()) {
                    settingsManager.setMockControlState(
                        settingsManager.mockControlStateFlow.first()
                            .copy(isWaitingAtEndOfRoute = true)
                    )

                    val finalPoint = anchorPoints.last()
                    while (mockControlState.value.isMocking) {
                        broadcastLocation(finalPoint, lastBroadcastLocation?.bearing ?: 0f, 0f)
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
