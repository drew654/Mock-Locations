package com.drew654.mocklocations.presentation

sealed class Screen(val route: String) {
    object Map : Screen("map")
    object Settings : Screen("settings")
}
