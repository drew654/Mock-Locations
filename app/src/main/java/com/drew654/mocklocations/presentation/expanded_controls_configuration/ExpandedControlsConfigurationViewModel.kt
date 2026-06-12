package com.drew654.mocklocations.presentation.expanded_controls_configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.ExpandedControlsConfigurationState
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpandedControlsConfigurationViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {
    private val _state = MutableStateFlow(ExpandedControlsConfigurationState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = ExpandedControlsConfigurationState(
                speedUnitValue = settingsManager.speedUnitValueFlow.first(),
                speedSliderLowerEnd = settingsManager.speedSliderLowerEndFlow.first().toString(),
                speedSliderUpperEnd = settingsManager.speedSliderUpperEndFlow.first().toString()
            )
        }
    }

    fun setIsShowingDialog(newValue: Boolean) {
        _state.value = _state.value.copy(isShowingDialog = newValue)
    }

    fun setSpeedSliderLowerEnd(newValue: String) {
        _state.value = _state.value.copy(speedSliderLowerEnd = newValue)
    }

    fun setSpeedSliderUpperEnd(newValue: String) {
        _state.value = _state.value.copy(speedSliderUpperEnd = newValue)
    }

    fun setSpeedUnitValue(newValue: SpeedUnitValue) {
        _state.value = _state.value.copy(speedUnitValue = newValue)
    }

    fun save(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val speedSliderLowerEnd = _state.value.speedSliderLowerEnd.toIntOrNull() ?: 0
            val speedSliderUpperEnd = _state.value.speedSliderUpperEnd.toIntOrNull() ?: 0
            var speedUnitValue = _state.value.speedUnitValue

            if (speedUnitValue.value < speedSliderLowerEnd) {
                speedUnitValue = speedUnitValue.copy(value = speedSliderLowerEnd.toDouble())
            } else if (speedUnitValue.value > speedSliderUpperEnd) {
                speedUnitValue = speedUnitValue.copy(value = speedSliderUpperEnd.toDouble())
            }

            settingsManager.setSpeedSettings(speedUnitValue, speedSliderLowerEnd, speedSliderUpperEnd)
            onSuccess()
        }
    }
}
