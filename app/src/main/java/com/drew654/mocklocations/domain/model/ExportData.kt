package com.drew654.mocklocations.domain.model

data class ExportData(
    val meta: ExportMeta,
    val settings: ExportSettings? = null,
    val routes: List<LocationTarget.SavedRoute>? = null
)
