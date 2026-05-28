package com.drew654.mocklocations.presentation.import_settings

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import com.drew654.mocklocations.domain.model.ImportRouteOption
import com.drew654.mocklocations.domain.model.ImportSettingsState
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ImportSettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<MockLocationsViewModel>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val importSettingsState = MutableStateFlow(ImportSettingsState())

    private fun setupMockFlows() {
        importSettingsState.value = ImportSettingsState()
        every { viewModel.importSettingsState } returns importSettingsState

        every { viewModel.updateImportSettingsState(any()) } answers {
            val transform = firstArg<(ImportSettingsState) -> ImportSettingsState>()
            importSettingsState.value = transform(importSettingsState.value)
        }
    }

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

    @Test
    fun radioButtonsHidden_whenNotImportRoutes() {
        composeTestRule.setContent {
            ImportSettingsContent(
                state = ImportSettingsState(
                    isImportRoutesEnabled = true,
                    routesToImport = 5
                )
            )
        }

        composeTestRule.onNodeWithText("Replace current routes").assertDoesNotExist()
        composeTestRule.onNodeWithText("Merge with current routes").assertDoesNotExist()
    }

    @Test
    fun radioButtonsShows_whenImportRoutesTrue() {
        composeTestRule.setContent {
            ImportSettingsContent(
                state = ImportSettingsState(
                    isImportRoutesEnabled = true,
                    isImportRoutes = true,
                    routesToImport = 5
                )
            )
        }

        composeTestRule.onNodeWithText("Replace current routes").assertExists()
        composeTestRule.onNodeWithText("Merge with current routes").assertExists()
    }

    @Test
    fun integration_onOpen_refreshImportSettingsState() {
        setupMockFlows()
        composeTestRule.setContent {
            ImportSettingsScreen(viewModel = viewModel, navController = navController)
        }

        verify { viewModel.refreshImportSettingsState() }
    }

    @Test
    fun integration_backButton_callsNavController() {
        setupMockFlows()
        composeTestRule.setContent {
            ImportSettingsScreen(viewModel = viewModel, navController = navController)
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        verify { navController.popBackStack() }
    }

    @Test
    fun integration_clickImportSettings_updatesViewModelAndUi() {
        setupMockFlows()
        composeTestRule.setContent {
            ImportSettingsScreen(viewModel = viewModel, navController = navController)
        }
        importSettingsState.value = ImportSettingsState(isImportSettingsEnabled = true)

        composeTestRule.onNodeWithText("Import settings").performClick()

        composeTestRule.onNodeWithTag("import_settings_checkbox").assertIsOn()
        assertTrue(importSettingsState.value.isImportSettings)
        composeTestRule.onNodeWithText("Import").assertIsEnabled()
    }

    @Test
    fun integration_clickImportRoutes_updatesViewModelAndUi() {
        setupMockFlows()
        composeTestRule.setContent {
            ImportSettingsScreen(viewModel = viewModel, navController = navController)
        }
        importSettingsState.value = ImportSettingsState(
            isImportRoutesEnabled = true,
            routesToImport = 5
        )

        composeTestRule.onNodeWithText("Import 5 routes").performClick()

        composeTestRule.onNodeWithTag("import_routes_checkbox").assertIsOn()
        assertTrue(importSettingsState.value.isImportRoutes)
        composeTestRule.onNodeWithText("Import").assertIsEnabled()
    }

    @Test
    fun integration_clickReplaceCurrentRoutes_updatesViewModelAndUi() {
        setupMockFlows()
        composeTestRule.setContent {
            ImportSettingsScreen(viewModel = viewModel, navController = navController)
        }
        importSettingsState.value = ImportSettingsState(
            isImportRoutesEnabled = true,
            isImportRoutes = true,
            importRouteOption = ImportRouteOption.MERGE,
            routesToImport = 5
        )

        composeTestRule.onNodeWithText("Replace current routes").performClick()

        composeTestRule.onNodeWithTag("replace_routes_radio_button").assertIsSelected()
        assert(importSettingsState.value.importRouteOption == ImportRouteOption.REPLACE)
        composeTestRule.onNodeWithText("Import").assertIsEnabled()
    }

    @Test
    fun integration_clickMergeWithCurrentRoutes_updatesViewModelAndUi() {
        setupMockFlows()
        composeTestRule.setContent {
            ImportSettingsScreen(viewModel = viewModel, navController = navController)
        }
        importSettingsState.value = ImportSettingsState(
            isImportRoutesEnabled = true,
            isImportRoutes = true,
            importRouteOption = ImportRouteOption.REPLACE,
            routesToImport = 5
        )

        composeTestRule.onNodeWithText("Merge with current routes").performClick()

        composeTestRule.onNodeWithTag("merge_routes_radio_button").assertIsSelected()
        assert(importSettingsState.value.importRouteOption == ImportRouteOption.MERGE)
        composeTestRule.onNodeWithText("Import").assertIsEnabled()
    }
}
