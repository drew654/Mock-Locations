package com.drew654.mocklocations.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.drew654.mocklocations.domain.model.Coordinates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockLocationsViewModel(application: Application) : AndroidViewModel(application) {
    private val _coordinates = MutableStateFlow<Coordinates?>(null)
    val coordinates: StateFlow<Coordinates?> = _coordinates.asStateFlow()

    fun setCoordinates(coordinates: Coordinates) {
        _coordinates.value = coordinates
    }
}
