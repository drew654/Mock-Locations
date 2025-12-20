package com.drew654.mocklocations.presentation

sealed class Screen(val route: String) {
    object Coordinates : Screen("coordinates")
    object Map : Screen("map")
}
