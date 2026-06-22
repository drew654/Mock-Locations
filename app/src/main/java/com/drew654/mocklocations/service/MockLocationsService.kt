package com.drew654.mocklocations.service

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
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.Permission
import com.drew654.mocklocations.domain.model.RoutePoint
import com.drew654.mocklocations.domain.model.isGranted
import com.drew654.mocklocations.domain.model.toMetersPerSecond
import com.drew654.mocklocations.presentation.toLatLng
import com.drew654.mocklocations.presentation.toRoutePoint
import com.drew654.mocklocations.util.LocationMathUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@AndroidEntryPoint
class MockLocationService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Inject
    lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var notificationHelper: MockNotificationHelper

    @Inject
    lateinit var locationManager: LocationManager

    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient

    private var mockJob: Job? = null
    private val providers =
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.FUSED_PROVIDER)

    private lateinit var mockControlState: StateFlow<MockControlState>
    private lateinit var accuracyMetersState: StateFlow<Float>
    private lateinit var locationUpdateDelayState: StateFlow<Long>
    private var lastBroadcastLocation: Location? = null
    private var noiseLat = 0.0
    private var noiseLng = 0.0

    companion object {
        const val ACTION_START_MOCKING = "ACTION_START_MOCKING"
        const val ACTION_STOP_MOCKING = "ACTION_STOP_MOCKING"
        const val ACTION_PAUSE_MOCKING = "ACTION_PAUSE_MOCKING_NOTIFICATION"
        const val ACTION_RESTORE_ROUTE_MOCKING = "ACTION_RESTORE_ROUTE_MOCKING"
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannel()

        mockControlState = settingsManager.mockControlStateFlow.stateIn(
            scope = serviceScope,
            started = SharingStarted.Eagerly,
            initialValue = MockControlState()
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
        } catch (e: Exception) {
            Log.e("MockLocationsService", "Failed to tear down test provider", e)
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

            ACTION_PAUSE_MOCKING -> {
                serviceScope.launch {
                    val current = settingsManager.mockControlStateFlow.first().isPaused
                    settingsManager.setMockControlState(settingsManager.mockControlStateFlow.first().copy(isPaused = !current))
                }
            }

            ACTION_RESTORE_ROUTE_MOCKING -> {
                serviceScope.launch {
                    val locationTarget = settingsManager.mockControlStateFlow.first().activeLocationTarget
                    val restoreMockingPoint = withTimeoutOrNull(3000) {
                        settingsManager.currentMockedLocationFlow
                            .filterNotNull()
                            .first()
                    }
                    if (locationTarget.isRoute()) {
                        if (restoreMockingPoint == null) {
                            mockLocationRoute(locationTarget)
                        } else {
                            restoreMockLocationRoute(locationTarget, restoreMockingPoint)
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

        val notification = notificationHelper.buildNotification(mockControlState)
        startForeground(
            MockNotificationHelper.NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )
    }

    private fun updateNoise() {
        val (newNoiseLat, newNoiseLng) = LocationMathUtils.getUpdatedNoiseLatLng(
            accuracyMetersState.value,
            noiseLat,
            noiseLng
        )
        noiseLat = newNoiseLat
        noiseLng = newNoiseLng
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
                    updateNoise()
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

        val (segmentIndex, distanceInSegment) = LocationMathUtils.findProgressOnRoute(
            points,
            restorePoint.latLng
        )

        startRouteMocking(
            anchorPoints = points,
            startSegmentIndex = segmentIndex,
            startDistanceInSegment = distanceInSegment,
            isStartedWaitingAtEndOfRoute = mockControlState.value.isWaitingAtEndOfRoute,
            startedMessage = "Route Mocking Restored"
        )
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
                                updateNoise()
                                continue
                            } else {
                                pausedBaseLocation = null
                            }

                            val fraction = (distanceInSegment / segmentLength).coerceIn(0.0, 1.0)
                            val currentPosition = LocationMathUtils.interpolate(start, end, fraction)

                            broadcastLocation(
                                currentPosition,
                                bearing,
                                currentSpeedMetersPerSec.toFloat()
                            )

                            delay(updateIntervalMs)
                            updateNoise()

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
                        updateNoise()
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
            } catch (e: Exception) {
                Log.e("MockLocationsService", "Failed to tear down test provider: $name", e)
            }
        }
    }

    private fun handleError(e: Exception) {
        if (e !is CancellationException) {
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
        withContext(NonCancellable) {
            tearDownTestProvider()
            settingsManager.setCurrentMockedLocation(null)
            lastBroadcastLocation = null

            val clearRouteOnStop = settingsManager.clearRouteOnStopFlow.first()
            val currentState = settingsManager.mockControlStateFlow.first()
            settingsManager.setMockControlState(
                currentState.copy(
                    isMocking = false,
                    isPaused = false,
                    isWaitingAtEndOfRoute = false,
                    isWaitingForRouteFetch = false,
                    activeLocationTarget = if (clearRouteOnStop) LocationTarget.Empty else currentState.activeLocationTarget
                )
            )

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private suspend fun stopMocking() {
        mockJob?.cancelAndJoin()
        mockJob = null
        stopMockingInternal()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
