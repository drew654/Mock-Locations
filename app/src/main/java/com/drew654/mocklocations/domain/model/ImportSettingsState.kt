package com.drew654.mocklocations.domain.model

import android.net.Uri

data class ImportSettingsState(
    val importUri: Uri? = null,
    val isImportRoutesEnabled: Boolean = false,
    val isImportRoutes: Boolean = false,
    val isImportSettingsEnabled: Boolean = false,
    val isImportSettings: Boolean = false,
    val importRouteOption: ImportRouteOption? = null,
    val routesToImport: Int = 0,
    val isImporting: Boolean = false
) {
    fun isFormValid(): Boolean {
        return (isImportRoutes || isImportSettings) && !isImporting
    }
}
