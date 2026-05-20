package com.drew654.mocklocations.domain.model

data class ImportSettingsState(
    val isImportRoutesEnabled: Boolean = false,
    val isImportRoutes: Boolean = false,
    val isImportSettingsEnabled: Boolean = false,
    val isImportSettings: Boolean = false,
    val importRouteOption: ImportRouteOption? = null,
    val routesToImport: Int = 0
) {
    fun isFormValid(): Boolean {
        return isImportRoutes || isImportSettings
    }
}
