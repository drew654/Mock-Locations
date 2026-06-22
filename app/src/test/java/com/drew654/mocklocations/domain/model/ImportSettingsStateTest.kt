package com.drew654.mocklocations.domain.model

import android.net.Uri
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ImportSettingsStateTest {
    @Test
    fun `data class properties are correctly initialized`() {
        val importUri: Uri? = null
        val isImportRoutesEnabled = true
        val isImportRoutes = true
        val isImportSettingsEnabled = true
        val isImportSettings = true
        val importRouteOption = ImportRouteOption.MERGE
        val routesToImport = 5

        val importSettingsState = ImportSettingsState(
            importUri = importUri,
            isImportRoutesEnabled = isImportRoutesEnabled,
            isImportRoutes = isImportRoutes,
            isImportSettingsEnabled = isImportSettingsEnabled,
            isImportSettings = isImportSettings,
            importRouteOption = importRouteOption,
            routesToImport = routesToImport
        )

        assertEquals(importUri, importSettingsState.importUri)
        assertEquals(isImportRoutesEnabled, importSettingsState.isImportRoutesEnabled)
        assertEquals(isImportRoutes, importSettingsState.isImportRoutes)
        assertEquals(isImportSettingsEnabled, importSettingsState.isImportSettingsEnabled)
        assertEquals(isImportSettings, importSettingsState.isImportSettings)
        assertEquals(importRouteOption, importSettingsState.importRouteOption)
        assertEquals(routesToImport, importSettingsState.routesToImport)
    }

    @Test
    fun `isFormValid returns true when isImportSettings is true`() {
        val importSettingsState = ImportSettingsState(
            isImportSettingsEnabled = true,
            isImportSettings = true
        )

        assertTrue(importSettingsState.isFormValid())
    }

    @Test
    fun `isFormValid returns true when isImportRoutes is true`() {
        val importSettingsState = ImportSettingsState(
            isImportRoutesEnabled = true,
            isImportRoutes = true,
            importRouteOption = ImportRouteOption.MERGE,
            routesToImport = 5
        )

        assertTrue(importSettingsState.isFormValid())
    }

    @Test
    fun `isFormValid returns false when isImportRoutes and isImportSettings are false`() {
        val importSettingsState = ImportSettingsState()

        assertFalse(importSettingsState.isFormValid())
    }
}
