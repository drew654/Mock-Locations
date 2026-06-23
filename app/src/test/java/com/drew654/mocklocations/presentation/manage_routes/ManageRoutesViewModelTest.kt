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
            name = "Route 1",
            routeSegments = listOf(
                RouteSegment(listOf(LatLng(0.0, 0.0))),
                RouteSegment(listOf(LatLng(1.0, 1.0)))
            )
        ),
        LocationTarget.SavedRoute(
            name = "Route 2",
            routeSegments = listOf(
                RouteSegment(listOf(LatLng(2.0, 2.0))),
                RouteSegment(listOf(LatLng(3.0, 3.0)))
            )
        ),
        LocationTarget.SavedRoute(
            name = "Route 3",
            routeSegments = listOf(
                RouteSegment(listOf(LatLng(4.0, 4.0))),
                RouteSegment(listOf(LatLng(5.0, 5.0)))
            )
        ),
        LocationTarget.SavedRoute(
            name = "Route 4",
            routeSegments = listOf(
                RouteSegment(listOf(LatLng(6.0, 6.0))),
                RouteSegment(listOf(LatLng(7.0, 7.0)))
            )
        ),
        LocationTarget.SavedRoute(
            name = "Route 5",
            routeSegments = listOf(
                RouteSegment(listOf(LatLng(8.0, 8.0))),
                RouteSegment(listOf(LatLng(9.0, 9.0)))
            )
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

    @Test
    fun `moveRouteUp moves selected route up`() = runTest {
        every { settingsManager.savedRoutesFlow } returns flowOf(sampleRoutes)
        viewModel = ManageRoutesViewModel(settingsManager)

        viewModel.setSelectedIndex(3)
        viewModel.moveRouteUp()

        assertEquals(2, viewModel.state.value.selectedIndex)
        assertEquals(sampleRoutes[0], viewModel.state.value.routes[0])
        assertEquals(sampleRoutes[1], viewModel.state.value.routes[1])
        assertEquals(sampleRoutes[2], viewModel.state.value.routes[3])
        assertEquals(sampleRoutes[3], viewModel.state.value.routes[2])
        assertEquals(sampleRoutes[4], viewModel.state.value.routes[4])
    }

    @Test
    fun `moveRouteUp with null selectedIndex does nothing`() = runTest {
        every { settingsManager.savedRoutesFlow } returns flowOf(sampleRoutes)
        viewModel = ManageRoutesViewModel(settingsManager)

        viewModel.moveRouteUp()

        assertNull(viewModel.state.value.selectedIndex)
        assertEquals(sampleRoutes, viewModel.state.value.routes)
    }

    @Test
    fun `moveRouteUp with first index does nothing`() = runTest {
        every { settingsManager.savedRoutesFlow } returns flowOf(sampleRoutes)
        viewModel = ManageRoutesViewModel(settingsManager)

        viewModel.setSelectedIndex(0)
        viewModel.moveRouteUp()

        assertEquals(sampleRoutes, viewModel.state.value.routes)
    }

    @Test
    fun `moveRouteDown moves selected route down`() = runTest {
        every { settingsManager.savedRoutesFlow } returns flowOf(sampleRoutes)
        viewModel = ManageRoutesViewModel(settingsManager)

        viewModel.setSelectedIndex(2)
        viewModel.moveRouteDown()

        assertEquals(3, viewModel.state.value.selectedIndex)
        assertEquals(sampleRoutes[0], viewModel.state.value.routes[0])
        assertEquals(sampleRoutes[1], viewModel.state.value.routes[1])
        assertEquals(sampleRoutes[3], viewModel.state.value.routes[2])
        assertEquals(sampleRoutes[2], viewModel.state.value.routes[3])
        assertEquals(sampleRoutes[4], viewModel.state.value.routes[4])
    }

    @Test
    fun `moveRouteDown with null selectedIndex does nothing`() = runTest {
        every { settingsManager.savedRoutesFlow } returns flowOf(sampleRoutes)
        viewModel = ManageRoutesViewModel(settingsManager)

        viewModel.moveRouteDown()

        assertNull(viewModel.state.value.selectedIndex)
        assertEquals(sampleRoutes, viewModel.state.value.routes)
    }

    @Test
    fun `moveRouteDown with last index does nothing`() = runTest {
        every { settingsManager.savedRoutesFlow } returns flowOf(sampleRoutes)
        viewModel = ManageRoutesViewModel(settingsManager)

        viewModel.setSelectedIndex(4)
        viewModel.moveRouteDown()

        assertEquals(sampleRoutes, viewModel.state.value.routes)
    }

    @Test
    fun `copyRoute copies selected route`() = runTest {
        every { settingsManager.savedRoutesFlow } returns flowOf(sampleRoutes)
        viewModel = ManageRoutesViewModel(settingsManager)

        viewModel.setSelectedIndex(2)
        viewModel.copyRoute()

        assertEquals(sampleRoutes[0], viewModel.state.value.routes[0])
        assertEquals(sampleRoutes[1], viewModel.state.value.routes[1])
        assertEquals(sampleRoutes[2], viewModel.state.value.routes[2])
        assertEquals(sampleRoutes[2].copy(name = "Copy of Route 3"), viewModel.state.value.routes[3])
        assertEquals(sampleRoutes[3], viewModel.state.value.routes[4])
        assertEquals(sampleRoutes[4], viewModel.state.value.routes[5])
    }

    @Test
    fun `copyRoute with null selectedIndex does nothing`() = runTest {
        every { settingsManager.savedRoutesFlow } returns flowOf(sampleRoutes)
        viewModel = ManageRoutesViewModel(settingsManager)

        viewModel.copyRoute()

        assertEquals(sampleRoutes, viewModel.state.value.routes)
    }

    @Test
    fun `copyRoute increments name if name already exists`() = runTest {
        every { settingsManager.savedRoutesFlow } returns flowOf(sampleRoutes)
        viewModel = ManageRoutesViewModel(settingsManager)

        viewModel.setSelectedIndex(2)
        viewModel.copyRoute()

        assertEquals(sampleRoutes[0], viewModel.state.value.routes[0])
        assertEquals(sampleRoutes[1], viewModel.state.value.routes[1])
        assertEquals(sampleRoutes[2], viewModel.state.value.routes[2])

        val copyName = "Copy of Route 3"
        assertEquals(sampleRoutes[2].copy(name = copyName), viewModel.state.value.routes[3])
        assertEquals(sampleRoutes[3], viewModel.state.value.routes[4])

        viewModel.copyRoute()

        assertEquals(sampleRoutes[0], viewModel.state.value.routes[0])
        assertEquals(sampleRoutes[1], viewModel.state.value.routes[1])
        assertEquals(sampleRoutes[2], viewModel.state.value.routes[2])

        val copyName2 = "Copy of Route 3 (1)"
        assertEquals(sampleRoutes[2].copy(name = copyName2), viewModel.state.value.routes[3])

        assertEquals(sampleRoutes[2].copy(name = copyName), viewModel.state.value.routes[4])
        assertEquals(sampleRoutes[3], viewModel.state.value.routes[5])
        assertEquals(sampleRoutes[4], viewModel.state.value.routes[6])

        viewModel.copyRoute()

        assertEquals(sampleRoutes[0], viewModel.state.value.routes[0])
        assertEquals(sampleRoutes[1], viewModel.state.value.routes[1])
        assertEquals(sampleRoutes[2], viewModel.state.value.routes[2])

        val copyName3 = "Copy of Route 3 (2)"
        assertEquals(sampleRoutes[2].copy(name = copyName3), viewModel.state.value.routes[3])

        assertEquals(sampleRoutes[2].copy(name = copyName2), viewModel.state.value.routes[4])
        assertEquals(sampleRoutes[2].copy(name = copyName), viewModel.state.value.routes[5])
        assertEquals(sampleRoutes[3], viewModel.state.value.routes[6])
        assertEquals(sampleRoutes[4], viewModel.state.value.routes[7])
    }
}
