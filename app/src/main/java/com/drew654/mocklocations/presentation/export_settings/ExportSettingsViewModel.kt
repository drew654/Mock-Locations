package com.drew654.mocklocations.presentation.export_settings

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.ExportSettingsState
import com.drew654.mocklocations.repository.ExportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ExportSettingsViewModel @Inject constructor(
    private val application: Application,
    private val settingsManager: SettingsManager,
    private val exportRepository: ExportRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ExportSettingsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val currentRoutesSize = settingsManager.savedRoutesFlow.first().size
            _state.value = ExportSettingsState(
                routesToExport = currentRoutesSize,
                isExportSettings = true,
                isExportRoutes = currentRoutesSize > 0
            )
        }
    }

    fun setIsExportSettings(newValue: Boolean) {
        _state.update { it.copy(isExportSettings = newValue) }
    }

    fun setIsExportRoutes(newValue: Boolean) {
        _state.update { it.copy(isExportRoutes = newValue) }
    }

    fun exportDataToUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonString = exportRepository.generateExportToJson(application, _state.value.isExportSettings, _state.value.isExportRoutes)
                application.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                    outputStream.flush()
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Export successful", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Export failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
