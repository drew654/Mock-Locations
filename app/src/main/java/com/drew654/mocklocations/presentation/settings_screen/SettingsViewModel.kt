package com.drew654.mocklocations.presentation.settings_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.LocationAccuracyLevel
import com.drew654.mocklocations.domain.model.MapStyle
import com.drew654.mocklocations.domain.model.SettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        settingsManager.buildRouteOnRoadsFlow.onEach { value ->
            _state.update { it.copy(isBuildRouteOnRoads = value) }
        }.launchIn(viewModelScope)

        settingsManager.mockControlStateFlow.onEach { value ->
            _state.update { it.copy(isUsingCrosshairs = value.isUsingCrosshairs) }
        }.launchIn(viewModelScope)

        settingsManager.clearRouteOnStopFlow.onEach { value ->
            _state.update { it.copy(clearPointsOnStop = value) }
        }.launchIn(viewModelScope)

        settingsManager.isCameraFollowingMockedLocation.onEach { value ->
            _state.update { it.copy(isCameraFollowingMockedLocation = value) }
        }.launchIn(viewModelScope)

        settingsManager.isGoingToWaitAtRouteFinishFlow.onEach { value ->
            _state.update { it.copy(isGoingToWaitAtRouteFinish = value) }
        }.launchIn(viewModelScope)

        settingsManager.mapStyleFlow.onEach { value ->
            _state.update { it.copy(mapStyle = value) }
        }.launchIn(viewModelScope)

        settingsManager.locationAccuracyLevelFlow.onEach { value ->
            _state.update { it.copy(locationAccuracyLevel = value) }
        }.launchIn(viewModelScope)

        settingsManager.locationUpdateDelayFlow.onEach { value ->
            _state.update { it.copy(locationUpdateDelay = value) }
        }.launchIn(viewModelScope)
    }

    fun setIsBuildRouteOnRoads(newValue: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isBuildRouteOnRoads = newValue) }
            settingsManager.setBuildRouteOnRoads(newValue)
        }
    }

    fun setIsUsingCrosshairs(newValue: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isUsingCrosshairs = newValue) }
            settingsManager.setIsUsingCrosshairs(newValue)
        }
    }

    fun setClearRouteOnStop(newValue: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(clearPointsOnStop = newValue) }
            settingsManager.setClearRouteOnStop(newValue)
        }
    }

    fun setIsCameraFollowingMockedLocation(newValue: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isCameraFollowingMockedLocation = newValue) }
            settingsManager.setIsCameraFollowingMockedLocation(newValue)
            settingsManager.setIsCameraCurrentlyFollowingMockedLocation(newValue)
        }
    }

    fun setIsGoingToWaitAtRouteFinish(newValue: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isGoingToWaitAtRouteFinish = newValue) }
            settingsManager.setIsGoingToWaitAtRouteFinish(newValue)
        }
    }

    fun setIsShowingMapStyleDialog(newValue: Boolean) {
        _state.update { it.copy(isShowingMapStyleDialog = newValue) }
    }

    fun setIsShowingLocationAccuracyDialog(newValue: Boolean) {
        _state.update { it.copy(isShowingLocationAccuracyLevelDialog = newValue) }
    }

    fun setIsShowingLocationUpdateDelayDialog(newValue: Boolean) {
        _state.update { it.copy( isShowingLocationUpdateDelayDialog = newValue) }
    }

    fun setIsShowingResetSettingsDialog(newValue: Boolean) {
        _state.update { it.copy(isShowingResetSettingsDialog = newValue) }
    }

    fun setMapStyle(newValue: MapStyle?) {
        viewModelScope.launch {
            _state.update { it.copy(mapStyle = newValue) }
            settingsManager.setMapStyle(newValue)
        }
    }

    fun setLocationAccuracyLevel(newValue: LocationAccuracyLevel) {
        viewModelScope.launch {
            _state.update { it.copy(locationAccuracyLevel = newValue) }
            settingsManager.setLocationAccuracyLevel(newValue)
        }
    }

    fun setLocationUpdateDelay(newValue: Float) {
        viewModelScope.launch {
            _state.update { it.copy(locationUpdateDelay = newValue) }
            settingsManager.setLocationUpdateDelay(newValue)
        }
    }

    fun resetSettingsToDefault() {
        viewModelScope.launch {
            _state.value = SettingsState()
            settingsManager.resetToDefault()
        }
    }
}
