package com.drew654.mocklocations.presentation.map_screen

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.MapStyle
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.Permission
import com.drew654.mocklocations.domain.model.RoutePoint
import com.drew654.mocklocations.domain.model.RouteSegment
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import com.drew654.mocklocations.repository.RouteRepository
import com.drew654.mocklocations.service.MockLocationService
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runCurrent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MapViewModelTest {
    private lateinit var application: Application
    private val settingsManager: SettingsManager = mockk(relaxed = true)
    private val routeRepository: RouteRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: MapViewModel
    
    private val mockControlStateFlow = MutableStateFlow(MockControlState())
    private val currentMockedLocationFlow = MutableStateFlow<RoutePoint?>(null)
    private val mapStyleFlow = MutableStateFlow<MapStyle?>(null)
    private val isCameraFollowingMockedLocationFlow = MutableStateFlow(true)
    private val isCameraCurrentlyFollowingMockedLocationFlow = MutableStateFlow(true)
    private val savedRoutesFlow = MutableStateFlow<List<LocationTarget.SavedRoute>>(emptyList())
    private val speedUnitValueFlow = MutableStateFlow(SpeedUnitValue(30.0, SpeedUnit.MilesPerHour))
    private val speedSliderLowerEndFlow = MutableStateFlow(0)
    private val speedSliderUpperEndFlow = MutableStateFlow(100)
    private val buildRouteOnRoadsFlow = MutableStateFlow(false)
    private val clearRouteOnStopFlow = MutableStateFlow(false)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        
        mockkStatic(MapStyleOptions::class)
        every { MapStyleOptions.loadRawResourceStyle(any(), any()) } returns mockk()

        every { settingsManager.mockControlStateFlow } returns mockControlStateFlow
        every { settingsManager.currentMockedLocationFlow } returns currentMockedLocationFlow
        every { settingsManager.mapStyleFlow } returns mapStyleFlow
        every { settingsManager.isCameraFollowingMockedLocation } returns isCameraFollowingMockedLocationFlow
        every { settingsManager.isCameraCurrentlyFollowingMockedLocationFlow } returns isCameraCurrentlyFollowingMockedLocationFlow
        every { settingsManager.savedRoutesFlow } returns savedRoutesFlow
        every { settingsManager.speedUnitValueFlow } returns speedUnitValueFlow
        every { settingsManager.speedSliderLowerEndFlow } returns speedSliderLowerEndFlow
        every { settingsManager.speedSliderUpperEndFlow } returns speedSliderUpperEndFlow
        every { settingsManager.buildRouteOnRoadsFlow } returns buildRouteOnRoadsFlow
        every { settingsManager.clearRouteOnStopFlow } returns clearRouteOnStopFlow

        coEvery { settingsManager.setMockControlState(any()) } coAnswers {
            mockControlStateFlow.value = it.invocation.args[0] as MockControlState
        }
        coEvery { settingsManager.setSpeedUnitValue(any()) } coAnswers {
            speedUnitValueFlow.value = it.invocation.args[0] as SpeedUnitValue
        }
        coEvery { settingsManager.setIsCameraCurrentlyFollowingMockedLocation(any()) } coAnswers {
            isCameraCurrentlyFollowingMockedLocationFlow.value = it.invocation.args[0] as Boolean
        }
        coEvery { settingsManager.saveRoute(any()) } returns Unit
        coEvery { settingsManager.deleteRoute(any()) } returns Unit

        viewModel = MapViewModel(application, settingsManager, routeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private suspend fun collectState(block: suspend () -> Unit) {
        coroutineScope {
            val job = launch { viewModel.state.collect {} }
            block()
            job.cancel()
        }
    }

    @Test
    fun `initial state is correct`() = runTest {
        collectState {
            val state = viewModel.state.value
            assertEquals(MockControlState(), state.mockControlState)
            assertEquals(null, state.currentMockedLocation)
            assertEquals(null, state.mapStyle)
            assertEquals(true, state.isCameraFollowingMockedLocation)
            assertEquals(true, state.isCameraCurrentlyFollowingMockedLocation)
            assertEquals(emptyList<LocationTarget.SavedRoute>(), state.savedRoutes)
        }
    }

    @Test
    fun `setHasLocationPermission updates state`() = runTest {
        collectState {
            viewModel.setHasLocationPermission(true)
            runCurrent()
            assertEquals(true, viewModel.state.value.hasLocationPermission)
        }
    }

    @Test
    fun `setControlsAreExpanded updates state`() = runTest {
        collectState {
            viewModel.setControlsAreExpanded(true)
            runCurrent()
            assertEquals(true, viewModel.state.value.expandedControlsState.isExpanded)

            viewModel.setControlsAreExpanded(false)
            runCurrent()
            assertEquals(false, viewModel.state.value.expandedControlsState.isExpanded)
        }
    }

    @Test
    fun `setSpeedValueUi updates state`() = runTest {
        collectState {
            val newSpeed = 45.0
            viewModel.setSpeedValueUi(newSpeed)
            runCurrent()
            assertEquals(newSpeed, viewModel.state.value.expandedControlsState.speedUnitValue.value, 0.0)
        }
    }

    @Test
    fun `saveSpeedUnitValue calls settingsManager`() = runTest {
        val speedValue = SpeedUnitValue(50.0, SpeedUnit.KilometersPerHour)
        viewModel.saveSpeedUnitValue(speedValue)
        runCurrent()
        assertEquals(speedValue, speedUnitValueFlow.value)
    }

    @Test
    fun `setPermissionToBeRequested updates state`() = runTest {
        collectState {
            val permission = Permission.FineLocation
            viewModel.setPermissionToBeRequested(permission)
            runCurrent()
            assertEquals(permission, viewModel.state.value.permissionToBeRequested)
        }
    }

    @Test
    fun `setAndSaveIsCameraCurrentlyFollowingMockedLocation updates state and settingsManager`() = runTest {
        collectState {
            viewModel.setAndSaveIsCameraCurrentlyFollowingMockedLocation(false)
            runCurrent()
            assertEquals(false, viewModel.state.value.isCameraCurrentlyFollowingMockedLocation)
            assertEquals(false, isCameraCurrentlyFollowingMockedLocationFlow.value)
        }
    }

    @Test
    fun `togglePause updates mockControlState and settingsManager`() = runTest {
        viewModel.togglePause()
        runCurrent()
        coVerify(atLeast = 1) { settingsManager.setMockControlState(any()) }
    }

    @Test
    fun `clearLocationTarget updates activeLocationTarget to Empty`() = runTest {
        viewModel.clearLocationTarget()
        runCurrent()
        coVerify(atLeast = 1) { settingsManager.setMockControlState(match { it.activeLocationTarget is LocationTarget.Empty }) }
    }

    @Test
    fun `stopMockLocation updates mockControlState and calls settingsManager`() = runTest {
        viewModel.stopMockLocation()
        runCurrent()
        coVerify(atLeast = 1) { settingsManager.setMockControlState(any()) }
        
        val intent = shadowOf(application).nextStartedService
        assertNotNull(intent)
        assertEquals(MockLocationService.ACTION_STOP_MOCKING, intent?.action)
    }

    @Test
    fun `stopMockLocation clears activeLocationTarget when clearRouteOnStop is true`() = runTest {
        clearRouteOnStopFlow.value = true
        val activeTarget = LocationTarget.SinglePoint(LatLng(1.0, 1.0))
        mockControlStateFlow.value = MockControlState(isMocking = true, activeLocationTarget = activeTarget)

        viewModel.stopMockLocation()
        runCurrent()

        assertEquals(LocationTarget.Empty, mockControlStateFlow.value.activeLocationTarget)
        assertEquals(false, mockControlStateFlow.value.isMocking)
    }

    @Test
    fun `saveCurrentRoute calls settingsManager saveRoute when activeLocationTarget is route`() = runTest {
        val routeSegments = listOf(RouteSegment(listOf(LatLng(0.0, 0.0), LatLng(1.0, 0.0))))
        val activeTarget = LocationTarget.SavedRoute(name = "Existing Route", routeSegments = routeSegments)
        mockControlStateFlow.value = MockControlState(activeLocationTarget = activeTarget)

        viewModel.saveCurrentRoute("Test Route")
        runCurrent()
        coVerify { settingsManager.saveRoute(any()) }
    }

    @Test
    fun `saveCurrentRoute does not call settingsManager saveRoute when activeLocationTarget is not a route`() = runTest {
        val activeTarget = LocationTarget.SinglePoint(LatLng(1.0, 1.0))
        mockControlStateFlow.value = MockControlState(activeLocationTarget = activeTarget)

        viewModel.saveCurrentRoute("Test Route")
        runCurrent()
        coVerify(exactly = 0) { settingsManager.saveRoute(any()) }
    }

    @Test
    fun `loadSavedRoute updates mockControlState`() = runTest {
        val route = LocationTarget.SavedRoute("Test Route", listOf(RouteSegment(listOf(LatLng(0.0, 0.0))), RouteSegment(listOf(LatLng(1.0, 0.0)))))
        viewModel.loadSavedRoute(route)
        runCurrent()
        coVerify(atLeast = 1) { settingsManager.setMockControlState(any()) }
        assertEquals(route, mockControlStateFlow.value.activeLocationTarget)
    }

    @Test
    fun `deleteSavedRoute calls settingsManager`() = runTest {
        val route = LocationTarget.SavedRoute("Test Route", listOf(RouteSegment(listOf(LatLng(0.0, 0.0))), RouteSegment(listOf(LatLng(1.0, 0.0)))))
        viewModel.deleteSavedRoute(route)
        runCurrent()
        coVerify { settingsManager.deleteRoute(route) }
    }

    @Test
    fun `pushRouteSegment adds point to activeLocationTarget when buildRouteOnRoads is false`() = runTest {
        val point = LatLng(1.0, 1.0)
        viewModel.pushRouteSegment(point)
        runCurrent()
        coVerify(atLeast = 1) { settingsManager.setMockControlState(any()) }
    }

    @Test
    fun `pushRouteSegment fetches route when buildRouteOnRoads is true`() = runTest {
        buildRouteOnRoadsFlow.value = true
        val point1 = LatLng(0.0, 0.0)
        val point2 = LatLng(0.5, 0.5)
        val endPoint = LatLng(1.0, 1.0)
        
        val routeSegments = listOf(
            RouteSegment(listOf(point1)),
            RouteSegment(listOf(point2))
        )
        val activeTarget = LocationTarget.SavedRoute("Existing Route", routeSegments)
        mockControlStateFlow.value = MockControlState(activeLocationTarget = activeTarget)
        
        val routePoints = listOf(point2, endPoint)
        coEvery { routeRepository.getRoutePoints(any(), any()) } returns routePoints
        
        viewModel.pushRouteSegment(endPoint)
        runCurrent()
        
        coVerify(exactly = 1) { routeRepository.getRoutePoints(point2, endPoint) }
        coVerify(atLeast = 1) { settingsManager.setMockControlState(any()) }
    }

    @Test
    fun `pushRouteSegment adds single point when buildRouteOnRoads is true and target is empty`() = runTest {
        buildRouteOnRoadsFlow.value = true
        mockControlStateFlow.value = MockControlState(activeLocationTarget = LocationTarget.Empty)
        
        val point = LatLng(1.0, 1.0)
        viewModel.pushRouteSegment(point)
        runCurrent()
        
        coVerify(atLeast = 1) { settingsManager.setMockControlState(match { 
            it.activeLocationTarget == LocationTarget.SinglePoint(point)
        }) }
        coVerify(exactly = 0) { routeRepository.getRoutePoints(any(), any()) }
    }

    @Test
    fun `popRouteSegment removes last segment`() = runTest {
        val segment1 = RouteSegment(listOf(LatLng(0.0, 0.0)))
        val segment2 = RouteSegment(listOf(LatLng(1.0, 1.0)))
        val activeTarget = LocationTarget.SavedRoute("Test Route", listOf(segment1, segment2))
        mockControlStateFlow.value = MockControlState(activeLocationTarget = activeTarget)

        viewModel.popRouteSegment()
        runCurrent()

        assertEquals(1, mockControlStateFlow.value.activeLocationTarget.routeSegments.size)
    }

    @Test
    fun `setCameraPosition updates state`() = runTest {
        collectState {
            val latLng = LatLng(10.0, 20.0)
            val cameraPosition = CameraPosition.fromLatLngZoom(latLng, 15f)
            viewModel.setCameraPosition(cameraPosition)
            runCurrent()

            val savedPosition = viewModel.state.value.savedCameraPosition
            assertNotNull(savedPosition)
            assertEquals(10.0, savedPosition?.latitude ?: 0.0, 0.0)
            assertEquals(20.0, savedPosition?.longitude ?: 0.0, 0.0)
            assertEquals(15f, savedPosition?.zoom ?: 0f)
        }
    }

    @Test
    fun `setHasRestoredCamera updates state`() = runTest {
        collectState {
            viewModel.setHasRestoredCamera(true)
            runCurrent()
            assertEquals(true, viewModel.state.value.hasRestoredCamera)
        }
    }

    @Test
    fun `setMapIsCenteredAfterLaunch updates state`() = runTest {
        collectState {
            viewModel.setMapIsCenteredAfterLaunch()
            runCurrent()
            assertEquals(true, viewModel.state.value.isMapCenteredAfterLaunch)
        }
    }

    @Test
    fun `setIsShowingSavedRoutesDialog updates state`() = runTest {
        collectState {
            viewModel.setIsShowingSavedRoutesDialog(true)
            runCurrent()
            assertEquals(true, viewModel.state.value.isShowingSavedRoutesDialog)
        }
    }

    @Test
    fun `setIsNamingRoute updates state`() = runTest {
        collectState {
            viewModel.setIsNamingRoute(true)
            runCurrent()
            assertEquals(true, viewModel.state.value.isNamingRoute)
        }
    }

    @Test
    fun `setIsShowingSearch updates state`() = runTest {
        collectState {
            viewModel.setIsShowingSearch(true)
            runCurrent()
            assertEquals(true, viewModel.state.value.isShowingSearch)
        }
    }

    @Test
    fun `setIsSystemInDarkTheme triggers state update`() = runTest {
        collectState {
            viewModel.setIsSystemInDarkTheme(true)
            runCurrent()
            verify { MapStyleOptions.loadRawResourceStyle(any(), any()) }
        }
    }

    @Test
    fun `startMockLocation starts mocking and service`() = runTest {
        val target = LatLng(5.0, 5.0)
        viewModel.startMockLocation(target)
        runCurrent()

        assertEquals(true, mockControlStateFlow.value.isMocking)
        
        val intent = shadowOf(application).nextStartedService
        assertNotNull(intent)
        assertEquals(MockLocationService.ACTION_START_MOCKING, intent?.action)
    }

    @Test
    fun `startMockLocation uses existing target if not empty`() = runTest {
        val existingPoint = LatLng(10.0, 10.0)
        val existingTarget = LocationTarget.SinglePoint(existingPoint)
        mockControlStateFlow.value = MockControlState(activeLocationTarget = existingTarget, isUsingCrosshairs = true)
        
        val cameraTarget = LatLng(5.0, 5.0)
        viewModel.startMockLocation(cameraTarget)
        runCurrent()

        assertEquals(true, mockControlStateFlow.value.isMocking)
        assertEquals(existingTarget, mockControlStateFlow.value.activeLocationTarget)
    }

    @Test
    fun `startMockLocation uses existing target if crosshairs disabled`() = runTest {
        mockControlStateFlow.value = MockControlState(activeLocationTarget = LocationTarget.Empty, isUsingCrosshairs = false)
        
        val cameraTarget = LatLng(5.0, 5.0)
        viewModel.startMockLocation(cameraTarget)
        runCurrent()

        assertEquals(true, mockControlStateFlow.value.isMocking)
        assertEquals(LocationTarget.Empty, mockControlStateFlow.value.activeLocationTarget)
    }

    @Test
    fun `pushRouteSegment shows toast when no route found`() = runTest {
        mockkStatic(android.widget.Toast::class)
        every { android.widget.Toast.makeText(any(), any<String>(), any()) } returns mockk(relaxed = true)

        buildRouteOnRoadsFlow.value = true
        val startPoint = LatLng(0.0, 0.0)
        val endPoint = LatLng(1.0, 1.0)
        val routeSegment = RouteSegment(listOf(startPoint))
        val activeTarget = LocationTarget.SavedRoute("Existing Route", listOf(routeSegment))
        mockControlStateFlow.value = MockControlState(activeLocationTarget = activeTarget)
        
        coEvery { routeRepository.getRoutePoints(any(), any()) } returns emptyList()
        
        viewModel.pushRouteSegment(endPoint)
        runCurrent()
        
        verify { android.widget.Toast.makeText(any(), "No route found", any()) }
    }

    @Test
    fun `init starts service if it was mocking a single point`() = runTest {
        val activeTarget = LocationTarget.SinglePoint(LatLng(1.0, 1.0))
        mockControlStateFlow.value = MockControlState(isMocking = true, activeLocationTarget = activeTarget)
        
        shadowOf(application).clearStartedServices()
        
        MapViewModel(application, settingsManager, routeRepository)
        runCurrent()
        
        val intent = shadowOf(application).nextStartedService
        assertNotNull(intent)
        assertEquals(MockLocationService.ACTION_START_MOCKING, intent?.action)
    }

    @Test
    fun `init restores route mocking if it was mocking a route`() = runTest {
        val routeSegments = listOf(RouteSegment(listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0))))
        val activeTarget = LocationTarget.Route(routeSegments)
        mockControlStateFlow.value = MockControlState(isMocking = true, activeLocationTarget = activeTarget)
        
        shadowOf(application).clearStartedServices()
        
        MapViewModel(application, settingsManager, routeRepository)
        runCurrent()
        
        val intent = shadowOf(application).nextStartedService
        assertNotNull(intent)
        assertEquals(MockLocationService.ACTION_RESTORE_ROUTE_MOCKING, intent?.action)
    }
}
