package com.drew654.mocklocations.presentation.expanded_controls_configuration

import com.drew654.mocklocations.MainDispatcherRule
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.drew654.mocklocations.domain.model.SpeedUnitValue
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
class ExpandedControlsConfigurationViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ExpandedControlsConfigurationViewModel
    private val settingsManager: SettingsManager = mockk(relaxed = true)

    private val initialSpeedUnitValue = SpeedUnitValue(30.0, SpeedUnit.MilesPerHour)
    private val initialLowerEnd = 0
    private val initialUpperEnd = 100

    @Before
    fun setUp() {
        every { settingsManager.speedUnitValueFlow } returns flowOf(initialSpeedUnitValue)
        every { settingsManager.speedSliderLowerEndFlow } returns flowOf(initialLowerEnd)
        every { settingsManager.speedSliderUpperEndFlow } returns flowOf(initialUpperEnd)

        viewModel = ExpandedControlsConfigurationViewModel(settingsManager)
    }

    @Test
    fun `init sets initial state from settingsManager`() = runTest {
        val state = viewModel.state.value
        assertEquals(initialSpeedUnitValue, state.speedUnitValue)
        assertEquals(initialLowerEnd.toString(), state.speedSliderLowerEnd)
        assertEquals(initialUpperEnd.toString(), state.speedSliderUpperEnd)
    }

    @Test
    fun `setIsShowingDialog updates state`() {
        viewModel.setIsShowingDialog(true)
        assertTrue(viewModel.state.value.isShowingDialog)

        viewModel.setIsShowingDialog(false)
        assertFalse(viewModel.state.value.isShowingDialog)
    }

    @Test
    fun `setSpeedSliderLowerEnd updates state`() {
        val newValue = "10"
        viewModel.setSpeedSliderLowerEnd(newValue)
        assertEquals(newValue, viewModel.state.value.speedSliderLowerEnd)
    }

    @Test
    fun `setSpeedSliderUpperEnd updates state`() {
        val newValue = "200"
        viewModel.setSpeedSliderUpperEnd(newValue)
        assertEquals(newValue, viewModel.state.value.speedSliderUpperEnd)
    }

    @Test
    fun `setSpeedUnitValue updates state`() {
        val newValue = SpeedUnitValue(50.0, SpeedUnit.KilometersPerHour)
        viewModel.setSpeedUnitValue(newValue)
        assertEquals(newValue, viewModel.state.value.speedUnitValue)
    }

    @Test
    fun `save calls settingsManager setSpeedSettings with correct values`() = runTest {
        val newLower = "10"
        val newUpper = "150"
        val newValue = SpeedUnitValue(60.0, SpeedUnit.MilesPerHour)

        viewModel.setSpeedSliderLowerEnd(newLower)
        viewModel.setSpeedSliderUpperEnd(newUpper)
        viewModel.setSpeedUnitValue(newValue)

        var successCalled = false
        viewModel.save { successCalled = true }

        coVerify {
            settingsManager.setSpeedSettings(newValue, 10, 150)
        }
        assertTrue(successCalled)
    }

    @Test
    fun `save clamps speed value to lower end if below`() = runTest {
        val lower = "20"
        val upper = "100"
        val value = SpeedUnitValue(10.0, SpeedUnit.MilesPerHour)

        viewModel.setSpeedSliderLowerEnd(lower)
        viewModel.setSpeedSliderUpperEnd(upper)
        viewModel.setSpeedUnitValue(value)

        var successCalled = false
        viewModel.save {
            successCalled = true
        }

        val expectedClampedValue = value.copy(value = 20.0)
        coVerify {
            settingsManager.setSpeedSettings(expectedClampedValue, 20, 100)
        }
        assertTrue(successCalled)
    }

    @Test
    fun `save clamps speed value to upper end if above`() = runTest {
        val lower = "0"
        val upper = "50"
        val value = SpeedUnitValue(60.0, SpeedUnit.MilesPerHour)

        viewModel.setSpeedSliderLowerEnd(lower)
        viewModel.setSpeedSliderUpperEnd(upper)
        viewModel.setSpeedUnitValue(value)

        var successCalled = false
        viewModel.save {
            successCalled = true
        }

        val expectedClampedValue = value.copy(value = 50.0)
        coVerify {
            settingsManager.setSpeedSettings(expectedClampedValue, 0, 50)
        }
        assertTrue(successCalled)
    }
}
