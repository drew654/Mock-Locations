package com.drew654.mocklocations.presentation.manage_routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.ManageRoutesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageRoutesViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {
    private val _state = MutableStateFlow(ManageRoutesState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val savedRoutes = settingsManager.savedRoutesFlow.first()

            _state.update {
                it.copy(
                    routes = savedRoutes
                )
            }
        }
    }

    fun setSelectedIndex(newValue: Int) {
        _state.update { it.copy(selectedIndex = newValue) }
    }

    fun deselectRoute() {
        _state.update { it.copy(selectedIndex = null) }
    }
}
