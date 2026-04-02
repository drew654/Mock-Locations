package com.drew654.mocklocations.data.repository

import android.content.Context
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.AccuracyLevel
import com.drew654.mocklocations.domain.model.ExportData
import com.drew654.mocklocations.domain.model.ExportMeta
import com.drew654.mocklocations.domain.model.ExportSettings
import com.drew654.mocklocations.domain.model.ImportRouteOption
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.getAccuracyLevelByName
import com.drew654.mocklocations.domain.model.getMapStyleByName
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
                useCrosshairs = settingsManager.mockControlStateFlow.first().isUsingCrosshairs,
                clearRouteOnStop = settingsManager.clearRouteOnStopFlow.first(),
                cameraFollowsMockedLocation = settingsManager.isCameraFollowingMockedLocation.first(),
                mapStyle = settingsManager.mapStyleFlow.first()?.name,
                expandedControlsSpeedUnitValue = settingsManager.speedUnitValueFlow.first(),
                expandedControlsSpeedSliderLowerEnd = settingsManager.speedSliderLowerEndFlow.first(),
                expandedControlsSpeedSliderUpperEnd = settingsManager.speedSliderUpperEndFlow.first(),
                waitAtRouteFinish = settingsManager.isGoingToWaitAtRouteFinishFlow.first(),
                accuracyLevel = settingsManager.accuracyLevelFlow.first().name,
                locationUpdateDelay = settingsManager.locationUpdateDelayFlow.first()
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

    suspend fun importFromJson(json: String, importSettings: Boolean, importRouteOption: ImportRouteOption?) {
        val exportData = settingsManager.gson.fromJson(json, ExportData::class.java)

        if (importSettings) {
            importSettings(exportData.settings)
        }
        if (importRouteOption != null) {
            importRoutes(exportData.routes, importRouteOption)
        }
    }

    fun getRouteCountFromJson(json: String): Int {
        val exportData = settingsManager.gson.fromJson(json, ExportData::class.java)
        return exportData.routes?.size ?: 0
    }

    fun isWithSettingsToImport(json: String): Boolean {
        val exportData = settingsManager.gson.fromJson(json, ExportData::class.java)
        return exportData.settings != null
    }

    private suspend fun importSettings(settings: ExportSettings?) {
        if (settings == null) return
        settingsManager.setMockControlState(settingsManager.mockControlStateFlow.first().copy(isUsingCrosshairs = settings.useCrosshairs))
        settingsManager.setClearRouteOnStop(settings.clearRouteOnStop)
        settingsManager.setIsCameraFollowingMockedLocation(settings.cameraFollowsMockedLocation)
        settingsManager.setMapStyle(getMapStyleByName(settings.mapStyle ?: ""))
        settingsManager.setSpeedUnitValue(settings.expandedControlsSpeedUnitValue)
        settingsManager.setSpeedSliderLowerEnd(settings.expandedControlsSpeedSliderLowerEnd)
        settingsManager.setSpeedSliderUpperEnd(settings.expandedControlsSpeedSliderUpperEnd)
        settingsManager.setIsGoingToWaitAtRouteFinish(settings.waitAtRouteFinish)
        settingsManager.setAccuracyLevel(getAccuracyLevelByName(settings.accuracyLevel) ?: AccuracyLevel.Perfect)
        settingsManager.setLocationUpdateDelay(settings.locationUpdateDelay)
    }

    private suspend fun importRoutes(routes: List<LocationTarget.SavedRoute>?, importOption: ImportRouteOption) {
        if (routes == null) return
        when (importOption) {
            ImportRouteOption.MERGE -> {
                settingsManager.mergeRoutes(routes)
            }
            ImportRouteOption.REPLACE -> {
                settingsManager.replaceRoutes(routes)
            }
        }
    }
}
