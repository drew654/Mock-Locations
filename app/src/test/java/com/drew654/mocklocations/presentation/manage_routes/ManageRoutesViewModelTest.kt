package com.drew654.mocklocations.presentation.manage_routes

import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.RouteSegment
import com.google.android.gms.maps.model.LatLng
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ManageRoutesViewModelTest {
    private val settingsManager: SettingsManager = mockk(relaxed = true)
    private lateinit var viewModel: ManageRoutesViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleRoutes = listOf(
        LocationTarget.SavedRoute(
            "Route 1",
            listOf(RouteSegment(listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0))))
        ),
        LocationTarget.SavedRoute(
            "Route 2",
            listOf(RouteSegment(listOf(LatLng(2.0, 2.0), LatLng(3.0, 3.0))))
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads routes from settingsManager`() = runTest {
        every { settingsManager.savedRoutesFlow } returns flowOf(sampleRoutes)
        
        viewModel = ManageRoutesViewModel(settingsManager)
        
        assertEquals(sampleRoutes, viewModel.state.value.routes)
    }

    @Test
    fun `init with empty routes loads empty list`() = runTest {
        every { settingsManager.savedRoutesFlow } returns flowOf(emptyList())
        
        viewModel = ManageRoutesViewModel(settingsManager)
        
        assertTrue(viewModel.state.value.routes.isEmpty())
    }

    @Test
    fun `setSelectedIndex updates state`() = runTest {
        every { settingsManager.savedRoutesFlow } returns flowOf(sampleRoutes)
        viewModel = ManageRoutesViewModel(settingsManager)

        val index = 1
        viewModel.setSelectedIndex(index)
        assertEquals(index, viewModel.state.value.selectedIndex)
    }

    @Test
    fun `deselectRoute sets selectedIndex to null`() = runTest {
        every { settingsManager.savedRoutesFlow } returns flowOf(sampleRoutes)
        viewModel = ManageRoutesViewModel(settingsManager)

        viewModel.setSelectedIndex(1)
        assertEquals(1, viewModel.state.value.selectedIndex)

        viewModel.deselectRoute()
        assertNull(viewModel.state.value.selectedIndex)
    }
}
