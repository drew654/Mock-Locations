package com.drew654.mocklocations.presentation.export_settings

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.drew654.mocklocations.domain.model.ExportSettingsState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ExportSettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickBackButton_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ExportSettingsContent(
                state = ExportSettingsState(),
                onBack = { clicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickExportSettings_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ExportSettingsContent(
                state = ExportSettingsState(),
                setIsExportSettings = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Export settings").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickExportRoutes_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ExportSettingsContent(
                state = ExportSettingsState(
                    routesToExport = 5,
                    isExportSettings = true,
                    isExportRoutes = true
                ),
                setIsExportRoutes = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Export 5 routes").performClick()

        assertTrue(clicked)
    }

    @Test
    fun clickExportRoutes_disabledWhenZeroRoutes() {
        var clicked = false
        composeTestRule.setContent {
            ExportSettingsContent(
                state = ExportSettingsState(),
                setIsExportRoutes = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Export 0 routes").performClick()

        assertFalse(clicked)
    }

    @Test
    fun clickExportButton_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ExportSettingsContent(
                state = ExportSettingsState(
                    routesToExport = 5,
                    isExportSettings = true,
                    isExportRoutes = true
                ),
                onExport = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Export").performClick()

        assertTrue(clicked)
    }

    @Test
    fun exportButton_disabledWhenFormInvalid() {
        var clicked = false
        composeTestRule.setContent {
            ExportSettingsContent(
                state = ExportSettingsState(),
                onExport = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Export").assertIsNotEnabled()
        assertFalse(clicked)
    }
}
