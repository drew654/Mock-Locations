package com.drew654.mocklocations.data.repository

import android.content.Context
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.ExportData
import com.drew654.mocklocations.domain.model.ExportMeta
import com.drew654.mocklocations.domain.model.ExportSettings
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.format.DateTimeFormatter

class ExportRepository(
    private val settingsManager: SettingsManager
) {
    suspend fun generateExportToJson(
        context: Context,
        exportSettings: Boolean,
        exportRoutes: Boolean
    ): String {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val meta = ExportMeta(
            appVersionName = packageInfo.versionName ?: "unknown",
            appVersionCode = packageInfo.longVersionCode.toInt(),
            exportedAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        )

        val settings = if (exportSettings) {
            ExportSettings(
                useCrosshairs = settingsManager.isUsingCrosshairsFlow.first(),
                clearRouteOnStop = settingsManager.clearRouteOnStopFlow.first(),
                cameraFollowsMockedLocation = settingsManager.isCameraFollowingMockedLocation.first(),
                mapStyle = settingsManager.mapStyleFlow.first()?.name,
                expandedControlsSpeedUnitValue = settingsManager.speedUnitValueFlow.first(),
                expandedControlsSpeedSliderLowerEnd = settingsManager.speedSliderLowerEndFlow.first(),
                expandedControlsSpeedSliderUpperEnd = settingsManager.speedSliderUpperEndFlow.first()
            )
        } else null

        val routes = if (exportRoutes) {
            settingsManager.savedRoutesFlow.first()
        } else null

        val exportData = ExportData(
            meta = meta,
            settings = settings,
            routes = routes
        )

        return settingsManager.gson.toJson(exportData)
    }
}
