package com.drew654.mocklocations.presentation.import_settings

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drew654.mocklocations.BuildConfig
import com.drew654.mocklocations.domain.model.ImportRouteOption
import com.drew654.mocklocations.domain.model.ImportSettingsState
import com.drew654.mocklocations.repository.ExportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ImportSettingsViewModel @Inject constructor(
    private val application: Application,
    private val exportRepository: ExportRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ImportSettingsState())
    val state = _state.asStateFlow()

    fun setImportUri(uri: Uri?) {
        _state.update { it.copy(importUri = uri) }
        if (uri != null) {
            refreshImportSummary(uri)
        }
    }

    private fun refreshImportSummary(uri: Uri) {
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    exportRepository.readJsonFromUri(application, uri)
                }

                val versionCode = exportRepository.getVersionCodeFromJson(json)
                if (versionCode > BuildConfig.VERSION_CODE) {
                    Toast.makeText(application, "App version is out of date", Toast.LENGTH_SHORT).show()
                    _state.update { it.copy(importUri = null) }
                    return@launch
                }

                val hasSettings = exportRepository.isWithSettingsToImport(json)
                val routeCount = exportRepository.getRouteCountFromJson(json)
                val hasRoutes = routeCount > 0

                _state.update {
                    it.copy(
                        isImportRoutesEnabled = hasRoutes,
                        isImportRoutes = hasRoutes,
                        isImportSettingsEnabled = hasSettings,
                        isImportSettings = hasSettings,
                        importRouteOption = if (hasRoutes) ImportRouteOption.REPLACE else null,
                        routesToImport = routeCount,
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ImportSettingsViewModel", "Failed to read file", e)
                Toast.makeText(application, "Failed to read file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setIsImportRoutes(newValue: Boolean) {
        _state.update { it.copy(isImportRoutes = newValue) }
    }

    fun setIsImportSettings(newValue: Boolean) {
        _state.update { it.copy(isImportSettings = newValue) }
    }

    fun setImportRouteOption(newValue: ImportRouteOption) {
        _state.update { it.copy(importRouteOption = newValue) }
    }

    fun importDataFromUri(onSuccess: () -> Unit) {
        val uri = _state.value.importUri ?: return
        _state.update { it.copy(isImporting = true) }
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    exportRepository.readJsonFromUri(application, uri)
                }

                withContext(Dispatchers.IO) {
                    exportRepository.importFromJson(
                        json,
                        _state.value.isImportSettings,
                        _state.value.importRouteOption
                    )
                }

                Toast.makeText(application, "Import successful", Toast.LENGTH_SHORT).show()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ImportSettingsViewModel", "Import failed", e)
                Toast.makeText(application, "Import failed", Toast.LENGTH_SHORT).show()
            } finally {
                _state.update { it.copy(isImporting = false) }
            }
        }
    }
}
