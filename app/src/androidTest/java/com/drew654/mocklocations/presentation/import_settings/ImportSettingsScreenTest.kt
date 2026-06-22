package com.drew654.mocklocations.presentation.import_settings

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
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
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import com.drew654.mocklocations.domain.model.ImportRouteOption
import com.drew654.mocklocations.domain.model.ImportSettingsState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ImportSettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<ImportSettingsViewModel>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val state = MutableStateFlow(ImportSettingsState())

    private fun setupMockFlows() {
        state.value = ImportSettingsState(importUri = mockk(relaxed = true))
        every { viewModel.state } returns state

        every { viewModel.setIsImportSettings(any()) } answers {
            state.value = state.value.copy(isImportSettings = firstArg())
        }

        every { viewModel.setIsImportRoutes(any()) } answers {
            state.value = state.value.copy(isImportRoutes = firstArg())
        }

        every { viewModel.setImportRouteOption(any()) } answers {
            state.value = state.value.copy(importRouteOption = firstArg())
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
                state = ImportSettingsState(
                    isImportRoutesEnabled = true,
                    isImportRoutes = true,
                    routesToImport = 5
                ),
                onImport = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Import").assertIsEnabled()
        composeTestRule.onNodeWithText("Import").performClick()

        assertTrue(clicked)
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

        composeTestRule.onNodeWithText("Replace current routes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Merge with current routes").assertIsDisplayed()
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
        state.value = state.value.copy(isImportSettingsEnabled = true)

        composeTestRule.onNodeWithText("Import settings").performClick()

        verify { viewModel.setIsImportSettings(true) }
        assertTrue(state.value.isImportSettings)
        composeTestRule.onNodeWithTag("import_settings_checkbox").assertIsOn()
        composeTestRule.onNodeWithText("Import").assertIsEnabled()
    }

    @Test
    fun integration_clickImportRoutes_updatesViewModelAndUi() {
        setupMockFlows()
        composeTestRule.setContent {
            ImportSettingsScreen(viewModel = viewModel, navController = navController)
        }
        state.value = state.value.copy(
            isImportRoutesEnabled = true,
            routesToImport = 5
        )

        composeTestRule.onNodeWithText("Import 5 routes").performClick()

        verify { viewModel.setIsImportRoutes(true) }
        assertTrue(state.value.isImportRoutes)
        composeTestRule.onNodeWithTag("import_routes_checkbox").assertIsOn()
        composeTestRule.onNodeWithText("Import").assertIsEnabled()
    }

    @Test
    fun integration_clickReplaceCurrentRoutes_updatesViewModelAndUi() {
        setupMockFlows()
        composeTestRule.setContent {
            ImportSettingsScreen(viewModel = viewModel, navController = navController)
        }
        state.value = state.value.copy(
            isImportRoutesEnabled = true,
            isImportRoutes = true,
            importRouteOption = ImportRouteOption.MERGE,
            routesToImport = 5
        )

        composeTestRule.onNodeWithText("Replace current routes").performClick()

        verify { viewModel.setImportRouteOption(ImportRouteOption.REPLACE) }
        assertEquals(state.value.importRouteOption, ImportRouteOption.REPLACE)
        composeTestRule.onNodeWithTag("replace_routes_radio_button").assertIsSelected()
        composeTestRule.onNodeWithText("Import").assertIsEnabled()
    }

    @Test
    fun integration_clickMergeWithCurrentRoutes_updatesViewModelAndUi() {
        setupMockFlows()
        composeTestRule.setContent {
            ImportSettingsScreen(viewModel = viewModel, navController = navController)
        }
        state.value = state.value.copy(
            isImportRoutesEnabled = true,
            isImportRoutes = true,
            importRouteOption = ImportRouteOption.REPLACE,
            routesToImport = 5
        )

        composeTestRule.onNodeWithText("Merge with current routes").performClick()

        verify { viewModel.setImportRouteOption(ImportRouteOption.MERGE) }
        assertEquals(state.value.importRouteOption, ImportRouteOption.MERGE)
        composeTestRule.onNodeWithTag("merge_routes_radio_button").assertIsSelected()
        composeTestRule.onNodeWithText("Import").assertIsEnabled()
    }

    @Test
    fun integration_launchesPicker_andSetsUriOnSuccess() {
        val testState = MutableStateFlow(ImportSettingsState(importUri = null))
        every { viewModel.state } returns testState
        val mockUri = mockk<Uri>()

        Intents.init()
        try {
            val resultIntent = Intent().apply { data = mockUri }
            intending(hasAction(Intent.ACTION_OPEN_DOCUMENT))
                .respondWith(ActivityResult(Activity.RESULT_OK, resultIntent))

            composeTestRule.setContent {
                ImportSettingsScreen(viewModel = viewModel, navController = navController)
            }

            intended(hasAction(Intent.ACTION_OPEN_DOCUMENT))
            verify { viewModel.setImportUri(mockUri) }
        } finally {
            Intents.release()
        }
    }

    @Test
    fun integration_launchesPicker_andPopsBackStackOnCancel() {
        val testState = MutableStateFlow(ImportSettingsState(importUri = null))
        every { viewModel.state } returns testState

        Intents.init()
        try {
            intending(hasAction(Intent.ACTION_OPEN_DOCUMENT))
                .respondWith(ActivityResult(Activity.RESULT_CANCELED, null))

            composeTestRule.setContent {
                ImportSettingsScreen(viewModel = viewModel, navController = navController)
            }

            intended(hasAction(Intent.ACTION_OPEN_DOCUMENT))
            verify { navController.popBackStack() }
        } finally {
            Intents.release()
        }
    }
}
