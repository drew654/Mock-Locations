package com.drew654.mocklocations.data.repository

import android.content.Context
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.LocationAccuracyLevel
import com.drew654.mocklocations.domain.model.ExportData
import com.drew654.mocklocations.domain.model.ExportMeta
import com.drew654.mocklocations.domain.model.ExportSettings
import com.drew654.mocklocations.domain.model.ImportRouteOption
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.getLocationAccuracyLevelByName
import com.drew654.mocklocations.domain.model.getMapStyleByName
import com.drew654.mocklocations.util.JsonUtils
import com.drew654.mocklocations.util.MigrationUtils
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.format.DateTimeFormatter

class ExportRepository(
    private val settingsManager: SettingsManager
) {
    val gson = JsonUtils.gson

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
                locationAccuracyLevel = settingsManager.locationAccuracyLevelFlow.first().name,
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

        return gson.toJson(exportData)
    }

    suspend fun importFromJson(json: String, importSettings: Boolean, importRouteOption: ImportRouteOption?) {
        var exportData = gson.fromJson(json, ExportData::class.java)

        if (exportData.meta.appVersionCode < 13) {
            val jsonObject = gson.fromJson(json, com.google.gson.JsonObject::class.java)
            if (jsonObject.has("routes")) {
                val legacyRoutesJson = jsonObject.get("routes").toString()
                val migratedRoutesJson = MigrationUtils.migrateSavedRoutesJsonTo13(legacyRoutesJson)
                val migratedRoutes = gson.fromJson(migratedRoutesJson, Array<LocationTarget.SavedRoute>::class.java).toList()
                exportData = exportData.copy(routes = migratedRoutes)
            }
        }

        if (importSettings) {
            importSettings(exportData.settings)
        }
        if (importRouteOption != null) {
            importRoutes(exportData.routes, importRouteOption)
        }
    }

    fun getRouteCountFromJson(json: String): Int {
        val exportData = gson.fromJson(json, ExportData::class.java)
        return exportData.routes?.size ?: 0
    }

    fun isWithSettingsToImport(json: String): Boolean {
        val exportData = gson.fromJson(json, ExportData::class.java)
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
        settingsManager.setLocationAccuracyLevel(getLocationAccuracyLevelByName(settings.locationAccuracyLevel) ?: LocationAccuracyLevel.Perfect)
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
