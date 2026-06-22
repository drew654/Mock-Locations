package com.drew654.mocklocations.presentation.settings_screen

import com.drew654.mocklocations.MainDispatcherRule
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.LocationAccuracyLevel
import com.drew654.mocklocations.domain.model.MapStyle
import com.drew654.mocklocations.domain.model.MockControlState
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settingsManager: SettingsManager = mockk(relaxed = true)
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        every { settingsManager.buildRouteOnRoadsFlow } returns flowOf(false)
        every { settingsManager.mockControlStateFlow } returns flowOf(MockControlState())
        every { settingsManager.clearRouteOnStopFlow } returns flowOf(false)
        every { settingsManager.isCameraFollowingMockedLocation } returns flowOf(true)
        every { settingsManager.isGoingToWaitAtRouteFinishFlow } returns flowOf(false)
        every { settingsManager.mapStyleFlow } returns flowOf(null)
        every { settingsManager.locationAccuracyLevelFlow } returns flowOf(LocationAccuracyLevel.Perfect)
        every { settingsManager.locationUpdateDelayFlow } returns flowOf(1f)

        viewModel = SettingsViewModel(settingsManager)
    }

    @Test
    fun `init sets initial state from settingsManager`() = runTest {
        val state = viewModel.state.value
        assertFalse(state.isBuildRouteOnRoads)
        assertTrue(state.isUsingCrosshairs)
        assertFalse(state.clearPointsOnStop)
        assertTrue(state.isCameraFollowingMockedLocation)
        assertFalse(state.isGoingToWaitAtRouteFinish)
        assertEquals(null, state.mapStyle)
        assertEquals(LocationAccuracyLevel.Perfect, state.locationAccuracyLevel)
        assertEquals(1f, state.locationUpdateDelay)
    }

    @Test
    fun `setIsBuildRouteOnRoads updates state and settingsManager`() = runTest {
        viewModel.setIsBuildRouteOnRoads(true)

        assertTrue(viewModel.state.value.isBuildRouteOnRoads)
        coVerify { settingsManager.setBuildRouteOnRoads(true) }

        viewModel.setIsBuildRouteOnRoads(false)

        assertFalse(viewModel.state.value.isBuildRouteOnRoads)
        coVerify { settingsManager.setBuildRouteOnRoads(false) }
    }

    @Test
    fun `setIsUsingCrosshairs updates state and settingsManager`() = runTest {
        viewModel.setIsUsingCrosshairs(true)

        assertTrue(viewModel.state.value.isUsingCrosshairs)
        coVerify { settingsManager.setIsUsingCrosshairs(true) }
        
        viewModel.setIsUsingCrosshairs(false)

        assertFalse(viewModel.state.value.isUsingCrosshairs)
        coVerify { settingsManager.setIsUsingCrosshairs(false) }
    }

    @Test
    fun `setClearRouteOnStop updates state and settingsManager`() = runTest {
        viewModel.setClearRouteOnStop(true)

        assertTrue(viewModel.state.value.clearPointsOnStop)
        coVerify { settingsManager.setClearRouteOnStop(true) }

        viewModel.setClearRouteOnStop(false)

        assertFalse(viewModel.state.value.clearPointsOnStop)
        coVerify { settingsManager.setClearRouteOnStop(false) }
    }

    @Test
    fun `setIsCameraFollowingMockedLocation updates state and settingsManager`() = runTest {
        viewModel.setIsCameraFollowingMockedLocation(true)

        assertTrue(viewModel.state.value.isCameraFollowingMockedLocation)
        coVerify { settingsManager.setIsCameraFollowingMockedLocation(true) }
        coVerify { settingsManager.setIsCameraCurrentlyFollowingMockedLocation(true) }
        
        
        viewModel.setIsCameraFollowingMockedLocation(false)

        assertFalse(viewModel.state.value.isCameraFollowingMockedLocation)
        coVerify { settingsManager.setIsCameraFollowingMockedLocation(false) }
        coVerify { settingsManager.setIsCameraCurrentlyFollowingMockedLocation(false) }
    }

    @Test
    fun `setIsGoingToWaitAtRouteFinish updates state and settingsManager`() = runTest {
        viewModel.setIsGoingToWaitAtRouteFinish(true)

        assertTrue(viewModel.state.value.isGoingToWaitAtRouteFinish)
        coVerify { settingsManager.setIsGoingToWaitAtRouteFinish(true) }

        viewModel.setIsGoingToWaitAtRouteFinish(false)

        assertFalse(viewModel.state.value.isGoingToWaitAtRouteFinish)
        coVerify { settingsManager.setIsGoingToWaitAtRouteFinish(false) }
    }

    @Test
    fun `setIsShowingMapStyleDialog updates state`() {
        viewModel.setIsShowingMapStyleDialog(true)
        assertTrue(viewModel.state.value.isShowingMapStyleDialog)

        viewModel.setIsShowingMapStyleDialog(false)
        assertFalse(viewModel.state.value.isShowingMapStyleDialog)
    }

    @Test
    fun `setIsShowingLocationAccuracyDialog updates state`() {
        viewModel.setIsShowingLocationAccuracyDialog(true)
        assertTrue(viewModel.state.value.isShowingLocationAccuracyLevelDialog)

        viewModel.setIsShowingLocationAccuracyDialog(false)
        assertFalse(viewModel.state.value.isShowingLocationAccuracyLevelDialog)
    }

    @Test
    fun `setIsShowingLocationUpdateDelayDialog updates state`() {
        viewModel.setIsShowingLocationUpdateDelayDialog(true)
        assertTrue(viewModel.state.value.isShowingLocationUpdateDelayDialog)

        viewModel.setIsShowingLocationUpdateDelayDialog(false)
        assertFalse(viewModel.state.value.isShowingLocationUpdateDelayDialog)
    }

    @Test
    fun `setIsShowingResetSettingsDialog updates state`() {
        viewModel.setIsShowingResetSettingsDialog(true)
        assertTrue(viewModel.state.value.isShowingResetSettingsDialog)

        viewModel.setIsShowingResetSettingsDialog(false)
        assertFalse(viewModel.state.value.isShowingResetSettingsDialog)
    }

    @Test
    fun `setMapStyle updates state and settingsManager`() = runTest {
        viewModel.setMapStyle(MapStyle.Night)

        assertEquals(MapStyle.Night, viewModel.state.value.mapStyle)
        coVerify { settingsManager.setMapStyle(MapStyle.Night) }
    }

    @Test
    fun `setLocationAccuracyLevel updates state and settingsManager`() = runTest {
        viewModel.setLocationAccuracyLevel(LocationAccuracyLevel.High)

        assertEquals(LocationAccuracyLevel.High, viewModel.state.value.locationAccuracyLevel)
        coVerify { settingsManager.setLocationAccuracyLevel(LocationAccuracyLevel.High) }
    }

    @Test
    fun `setLocationUpdateDelay updates state and settingsManager`() = runTest {
        viewModel.setLocationUpdateDelay(2f)

        assertEquals(2f, viewModel.state.value.locationUpdateDelay)
        coVerify { settingsManager.setLocationUpdateDelay(2f) }
    }

    @Test
    fun `resetSettingsToDefault updates state and settingsManager`() = runTest {
        viewModel.setIsBuildRouteOnRoads(true)
        viewModel.setIsUsingCrosshairs(false)
        viewModel.setClearRouteOnStop(true)
        viewModel.setIsCameraFollowingMockedLocation(false)
        viewModel.setIsGoingToWaitAtRouteFinish(true)
        viewModel.setMapStyle(MapStyle.Night)
        viewModel.setLocationAccuracyLevel(LocationAccuracyLevel.High)
        viewModel.setLocationUpdateDelay(2f)
        
        viewModel.resetSettingsToDefault()

        val state = viewModel.state.value
        assertFalse(state.isBuildRouteOnRoads)
        assertTrue(state.isUsingCrosshairs)
        assertFalse(state.clearPointsOnStop)
        assertTrue(state.isCameraFollowingMockedLocation)
        assertFalse(state.isGoingToWaitAtRouteFinish)
        assertEquals(null, state.mapStyle)
        assertEquals(LocationAccuracyLevel.Perfect, state.locationAccuracyLevel)
        assertEquals(1f, state.locationUpdateDelay)
        
        coVerify { settingsManager.resetToDefault() }
    }
}
