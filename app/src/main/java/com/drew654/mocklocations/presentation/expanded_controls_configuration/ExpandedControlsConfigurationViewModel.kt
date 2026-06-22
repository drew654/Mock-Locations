package com.drew654.mocklocations.presentation.expanded_controls_configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.ExpandedControlsConfigurationState
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpandedControlsConfigurationViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {
    private val _state = MutableStateFlow(ExpandedControlsConfigurationState())
    val state = _state.asStateFlow()

    init {
        settingsManager.speedUnitValueFlow.onEach { value ->
            _state.update { it.copy(speedUnitValue = value) }
        }.launchIn(viewModelScope)

        settingsManager.speedSliderLowerEndFlow.onEach { value ->
            _state.update { it.copy(speedSliderLowerEnd = value.toString()) }
        }.launchIn(viewModelScope)

        settingsManager.speedSliderUpperEndFlow.onEach { value ->
            _state.update { it.copy(speedSliderUpperEnd = value.toString()) }
        }.launchIn(viewModelScope)
    }

    fun setIsShowingDialog(newValue: Boolean) {
        _state.update { it.copy(isShowingDialog = newValue) }
    }

    fun setSpeedSliderLowerEnd(newValue: String) {
        _state.update { it.copy(speedSliderLowerEnd = newValue) }
    }

    fun setSpeedSliderUpperEnd(newValue: String) {
        _state.update { it.copy(speedSliderUpperEnd = newValue) }
    }

    fun setSpeedUnitValue(newValue: SpeedUnitValue) {
        _state.update { it.copy(speedUnitValue = newValue) }
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
