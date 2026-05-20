package com.drew654.mocklocations.domain.model

data class ExportSettingsState(
    val routesToExport: Int = 0,
    val isExportSettings: Boolean = false,
    val isExportRoutes: Boolean = false
)
