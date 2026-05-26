package com.drew654.mocklocations.presentation.import_settings

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.drew654.mocklocations.domain.model.ImportSettingsState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ImportSettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickBackButton_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ImportSettingsContent(
                state = ImportSettingsState(),
                onBack = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickImportSettings_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ImportSettingsContent(
                state = ImportSettingsState(
                    isImportSettingsEnabled = true
                ),
                setIsImportSettings = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Import settings").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickImportSettings_disabled_whenStateDisabled() {
        var clicked = false
        composeTestRule.setContent {
            ImportSettingsContent(
                state = ImportSettingsState(),
                setIsImportSettings = { clicked = true }
            )
        }

        composeTestRule.onNodeWithTag("import_settings_checkbox").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Import settings").performClick()

        assertFalse(clicked)
    }

    @Test
    fun clickImportRoutes_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ImportSettingsContent(
                state = ImportSettingsState(
                    isImportRoutesEnabled = true,
                    routesToImport = 5
                ),
                setIsImportRoutes = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Import 5 routes").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickImportRoutes_disabled_whenStateDisabled() {
        var clicked = false
        composeTestRule.setContent {
            ImportSettingsContent(
                state = ImportSettingsState(),
                setIsImportRoutes = { clicked = true }
            )
        }

        composeTestRule.onNodeWithTag("import_routes_checkbox").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Import 0 routes").performClick()

        assertFalse(clicked)
    }

    @Test
    fun clickImportButton_tiggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ImportSettingsContent(
                state = ImportSettingsState(),
                onImport = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Import").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Import").performClick()

        assertFalse(clicked)
    }

    @Test
    fun clickImportButton_disabled_whenFormInvalid() {
        var clicked = false
        composeTestRule.setContent {
            ImportSettingsContent(
                state = ImportSettingsState(),
                onImport = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Import").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Import").performClick()

        assertFalse(clicked)
    }
}
