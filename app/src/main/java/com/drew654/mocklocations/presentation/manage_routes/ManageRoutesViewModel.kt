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

    fun setSelectedRoute(name: String) {
        _state.update { it.copy(selectedRouteName = name) }
    }

    fun deselectRoute() {
        _state.update { it.copy(selectedRouteName = null) }
    }

    fun moveRouteUp() {
        val selectedName = state.value.selectedRouteName ?: return
        val routes = state.value.routes.toMutableList()
        val index = routes.indexOfFirst { it.name == selectedName }

        if (index != -1 && index > 0) {
            val temp = routes[index - 1]
            routes[index - 1] = routes[index]
            routes[index] = temp
            _state.update { it.copy(routes = routes) }
            viewModelScope.launch {
                settingsManager.replaceRoutes(routes)
            }
        }
    }

    fun moveRouteDown() {
        val selectedName = state.value.selectedRouteName ?: return
        val routes = state.value.routes.toMutableList()
        val index = routes.indexOfFirst { it.name == selectedName }

        if (index != -1 && index < routes.size - 1) {
            val temp = routes[index + 1]
            routes[index + 1] = routes[index]
            routes[index] = temp
            _state.update { it.copy(routes = routes) }
            viewModelScope.launch {
                settingsManager.replaceRoutes(routes)
            }
        }
    }

    fun onEditClicked(name: String) {
        _state.update { it.copy(editRouteName = name) }
    }

    fun onRouteNameChanged(name: String) {
        _state.update { it.copy(editRouteName = name) }
    }

    fun onRouteRenameDismissed() {
        _state.update { it.copy(editRouteName = null) }
    }

    fun onRouteRenameConfirmed() {
        val selectedRoute =
            state.value.routes.find { it.name == state.value.selectedRouteName } ?: return

        val updatedRoute = selectedRoute.copy(
            name = state.value.editRouteName ?: ""
        )

        val routes = state.value.routes.toMutableList()
        val index = routes.indexOf(selectedRoute)

        routes[index] = updatedRoute

        val updatedRoutes = routes.toList()

        _state.update {
            it.copy(
                routes = updatedRoutes,
                editRouteName = null
            )
        }

        viewModelScope.launch {
            settingsManager.replaceRoutes(updatedRoutes)
        }
    }

    fun copyRoute() {
        val selectedName = state.value.selectedRouteName ?: return
        val routes = state.value.routes.toMutableList()
        val index = routes.indexOfFirst { it.name == selectedName }
        if (index == -1) return

        var newName = "Copy of ${routes[index].name}"
        var copyIndex = 1
        while (routes.any { it.name == newName }) {
            newName = "Copy of ${routes[index].name} ($copyIndex)"
            copyIndex++
        }
        routes.add(index + 1, routes[index].copy(name = newName))
        _state.update { it.copy(routes = routes) }
        viewModelScope.launch {
            settingsManager.replaceRoutes(routes)
        }
    }

    fun deleteRoute() {
        val selectedName = state.value.selectedRouteName ?: return
        val routes = state.value.routes.toMutableList()
        val routeToDelete = routes.find { it.name == selectedName } ?: return
        val updatedRoutes = routes.minus(routeToDelete)
        _state.update { it.copy(routes = updatedRoutes) }
        viewModelScope.launch {
            settingsManager.replaceRoutes(updatedRoutes)
        }
    }
}
