package com.drew654.mocklocations.presentation.export_settings

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import com.drew654.mocklocations.domain.model.ExportSettingsState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ExportSettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<ExportSettingsViewModel>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val state = MutableStateFlow(ExportSettingsState())

    private fun setupMockFlows() {
        state.value = ExportSettingsState()
        every { viewModel.state } returns state

        every { viewModel.setIsExportSettings(any()) } answers {
            state.value = state.value.copy(isExportSettings = firstArg())
        }

        every { viewModel.setIsExportRoutes(any()) } answers {
            state.value = state.value.copy(isExportRoutes = firstArg())
        }
    }

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
    fun clickExportRoutes_disabled_whenZeroRoutes() {
        var clicked = false
        composeTestRule.setContent {
            ExportSettingsContent(
                state = ExportSettingsState(),
                setIsExportRoutes = { clicked = true }
            )
        }

        composeTestRule.onNodeWithTag("export_routes_checkbox").assertIsNotEnabled()
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
    fun clickExportButton_disabled_whenFormInvalid() {
        var clicked = false
        composeTestRule.setContent {
            ExportSettingsContent(
                state = ExportSettingsState(),
                onExport = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Export").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Export").performClick()

        assertFalse(clicked)
    }

    @Test
    fun integration_backButton_callsNavController() {
        setupMockFlows()
        composeTestRule.setContent {
            ExportSettingsScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        verify { navController.popBackStack() }
    }

    @Test
    fun integration_clickExportSettings_updatesViewModelAndUi() {
        setupMockFlows()
        composeTestRule.setContent {
            ExportSettingsScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithText("Export settings").performClick()

        verify { viewModel.setIsExportSettings(true) }
        assertTrue(state.value.isExportSettings)
        composeTestRule.onNodeWithTag("export_settings_checkbox").assertIsOn()
        composeTestRule.onNodeWithText("Export").assertIsEnabled()
    }

    @Test
    fun integration_clickMultipleSettings_updatesViewModelAndUi() {
        setupMockFlows()
        composeTestRule.setContent {
            ExportSettingsScreen(viewModel = viewModel, navController = navController)
        }
        state.value = ExportSettingsState(
            routesToExport = 5
        )

        composeTestRule.onNodeWithText("Export settings").performClick()

        verify { viewModel.setIsExportSettings(true) }
        assertTrue(state.value.isExportSettings)
        composeTestRule.onNodeWithTag("export_settings_checkbox").assertIsOn()
        composeTestRule.onNodeWithText("Export").assertIsEnabled()

        composeTestRule.onNodeWithText("Export 5 routes").performClick()

        verify { viewModel.setIsExportRoutes(true) }
        assertTrue(state.value.isExportRoutes)
        composeTestRule.onNodeWithTag("export_routes_checkbox").assertIsOn()
        composeTestRule.onNodeWithText("Export").assertIsEnabled()
    }

    @Test
    fun integration_clickExportRoutes_updatesViewModelAndUi() {
        setupMockFlows()
        composeTestRule.setContent {
            ExportSettingsScreen(viewModel = viewModel, navController = navController)
        }
        state.value = ExportSettingsState(
            routesToExport = 5
        )

        composeTestRule.onNodeWithText("Export 5 routes").performClick()

        verify { viewModel.setIsExportRoutes(true) }
        assertTrue(state.value.isExportRoutes)
        composeTestRule.onNodeWithTag("export_routes_checkbox").assertIsOn()
        composeTestRule.onNodeWithText("Export").assertIsEnabled()
    }
}
