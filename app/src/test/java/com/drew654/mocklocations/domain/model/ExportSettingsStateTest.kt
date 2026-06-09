package com.drew654.mocklocations.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportSettingsStateTest {
    @Test
    fun `data class properties are correctly initialized`() {
        val routesToExport = 5
        val isExportSettings = true
        val isExportRoutes = true

        val exportSettingsState = ExportSettingsState(
            routesToExport = routesToExport,
            isExportSettings = isExportSettings,
            isExportRoutes = isExportRoutes
        )

        assertEquals(routesToExport, exportSettingsState.routesToExport)
        assertEquals(isExportSettings, exportSettingsState.isExportSettings)
        assertEquals(isExportRoutes, exportSettingsState.isExportRoutes)
    }

    @Test
    fun `isFormValid returns true when isExportSettings is true`() {
        val exportSettingsState = ExportSettingsState(
            isExportSettings = true
        )

        assertTrue(exportSettingsState.isFormValid())
    }

    @Test
    fun `isFormValid returns true when isExportRoutes is true`() {
        val exportSettingsState = ExportSettingsState(
            routesToExport = 5,
            isExportRoutes = true
        )

        assertTrue(exportSettingsState.isFormValid())
    }

    @Test
    fun `isFormValid returns false when isExportSettings and isExportRoutes are false`() {
        val exportSettingsState = ExportSettingsState(
            routesToExport = 0,
            isExportSettings = false,
            isExportRoutes = false
        )

        assertFalse(exportSettingsState.isFormValid())
    }
}
