package com.drew654.mocklocations.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpandedControlsConfigurationStateTest {
    @Test
    fun `data class properties are correctly initialized`() {
        val isShowingDialog = true
        val speedUnitValue = SpeedUnitValue(
            value = 50.0,
            speedUnit = SpeedUnit.KilometersPerHour
        )
        val speedSliderLowerEnd = "50"
        val speedSliderUpperEnd = "200"

        val expandedControlsConfigurationState = ExpandedControlsConfigurationState(
            isShowingDialog = isShowingDialog,
            speedUnitValue = speedUnitValue,
            speedSliderLowerEnd = speedSliderLowerEnd,
            speedSliderUpperEnd = speedSliderUpperEnd
        )

        assertEquals(isShowingDialog, expandedControlsConfigurationState.isShowingDialog)
        assertEquals(speedUnitValue, expandedControlsConfigurationState.speedUnitValue)
        assertEquals(speedSliderLowerEnd, expandedControlsConfigurationState.speedSliderLowerEnd)
        assertEquals(speedSliderUpperEnd, expandedControlsConfigurationState.speedSliderUpperEnd)
    }

    @Test
    fun `isFormValid returns false when speedSliderLowerEnd is not an Int`() {
        val expandedControlsConfigurationState = ExpandedControlsConfigurationState(
            speedSliderLowerEnd = "50.5"
        )

        assertFalse(expandedControlsConfigurationState.isFormValid())
    }

    @Test
    fun `isFormValid returns false when speedSliderUpperEnd is not an Int`() {
        val expandedControlsConfigurationState = ExpandedControlsConfigurationState(
            speedSliderUpperEnd = "200.5"
        )

        assertFalse(expandedControlsConfigurationState.isFormValid())
    }

    @Test
    fun `isFormValid returns false when speedSliderLowerEnd is negative`() {
        val expandedControlsConfigurationState = ExpandedControlsConfigurationState(
            speedSliderLowerEnd = "-50"
        )

        assertFalse(expandedControlsConfigurationState.isFormValid())
    }

    @Test
    fun `isFormValid returns false when speedSliderLowerEnd is greater than speedSliderUpperEnd`() {
        val expandedControlsConfigurationState = ExpandedControlsConfigurationState(
            speedSliderLowerEnd = "100",
            speedSliderUpperEnd = "50"
        )

        assertFalse(expandedControlsConfigurationState.isFormValid())
    }

    @Test
    fun `isFormValid returns true with valid values`() {
        val expandedControlsConfigurationState = ExpandedControlsConfigurationState(
            speedSliderLowerEnd = "50",
            speedSliderUpperEnd = "200"
        )

        assertTrue(expandedControlsConfigurationState.isFormValid())
    }
}
