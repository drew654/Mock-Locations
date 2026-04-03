package com.drew654.mocklocations.presentation

sealed class Screen(val route: String) {
    object Map : Screen("map")
    object Settings : Screen("settings")
    object ExpandedControlsConfiguration : Screen("expanded_controls_configuration")
    object ExportSettings : Screen("export_settings")
    object ImportSettings : Screen("import_settings")
}
