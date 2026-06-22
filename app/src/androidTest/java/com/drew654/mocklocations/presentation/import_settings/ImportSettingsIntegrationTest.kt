package com.drew654.mocklocations.presentation.import_settings

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import com.drew654.mocklocations.presentation.Screen
import com.drew654.mocklocations.presentation.expanded_controls_configuration.ExpandedControlsConfigurationScreen
import com.drew654.mocklocations.presentation.expanded_controls_configuration.ExpandedControlsConfigurationViewModel
import com.drew654.mocklocations.presentation.settings_screen.SettingsScreen
import com.drew654.mocklocations.presentation.settings_screen.SettingsViewModel
import com.drew654.mocklocations.repository.ExportRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class ImportSettingsIntegrationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var expandedControlsConfigurationViewModel: ExpandedControlsConfigurationViewModel
    private lateinit var importViewModel: ImportSettingsViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var exportRepository: ExportRepository
    private lateinit var settingsManager: SettingsManager
    private lateinit var context: Application

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        settingsManager = SettingsManager(context)
        exportRepository = ExportRepository(settingsManager)
        expandedControlsConfigurationViewModel = ExpandedControlsConfigurationViewModel(settingsManager)
        importViewModel = ImportSettingsViewModel(context, exportRepository)
        settingsViewModel = SettingsViewModel(settingsManager)

        runBlocking {
            settingsManager.resetToDefault()
            settingsManager.setIsUsingCrosshairs(false)
            settingsManager.setIsCameraFollowingMockedLocation(false)
            settingsManager.setSpeedUnitValue(SpeedUnitValue(12.0, SpeedUnit.MilesPerHour))
        }
    }

    @Test
    fun importSettings_updatesUiSummary() {
        val assetManager = InstrumentationRegistry.getInstrumentation().context.assets
        val json = assetManager.open("18_mock_locations_2026_06_22_10_38_12.json").bufferedReader().use { it.readText() }

        val tempFile = File(context.cacheDir, "temp_import.json")
        tempFile.writeText(json)
        val uri = Uri.fromFile(tempFile)

        importViewModel.setImportUri(uri)

        composeTestRule.setContent {
            val state by importViewModel.state.collectAsState()
            ImportSettingsContent(state = state)
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            importViewModel.state.value.routesToImport > 0
        }

        composeTestRule.onNodeWithText("Import settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Import 4 routes").assertIsDisplayed()
    }

    @Test
    fun importSettings_navigatesBackToSettings_andShowsUpdatedContent() {
        val assetManager = InstrumentationRegistry.getInstrumentation().context.assets
        val json = assetManager.open("18_mock_locations_2026_06_22_10_38_12.json").bufferedReader().use { it.readText() }
        val tempFile = File(context.cacheDir, "temp_import.json")
        tempFile.writeText(json)
        val uri = Uri.fromFile(tempFile)

        importViewModel.setImportUri(uri)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = Screen.Settings.route) {
                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = settingsViewModel, navController = navController)
                }
                composable(Screen.ImportSettings.route) {
                    ImportSettingsScreen(viewModel = importViewModel, navController = navController)
                }
            }
        }

        composeTestRule.onNodeWithText("Import settings").performScrollTo().performClick()
        
        composeTestRule.onNodeWithText("Import Settings").assertIsDisplayed()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            importViewModel.state.value.isImportSettingsEnabled
        }

        composeTestRule.onNodeWithText("Import").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Settings").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithTag("build_route_on_roads_switch").assertIsOn()
                composeTestRule.onNodeWithTag("use_crosshairs_switch").assertIsOn()
                composeTestRule.onNodeWithTag("clear_route_on_stop_switch").assertIsOn()
                composeTestRule.onNodeWithTag("camera_follows_mocked_location_switch").assertIsOn()
                composeTestRule.onNodeWithTag("wait_at_the_end_of_a_route_switch").assertIsOn()
                composeTestRule.onNodeWithText("Terrain").performScrollTo().assertIsDisplayed()
                composeTestRule.onNodeWithText("Low").performScrollTo().assertIsDisplayed()
                composeTestRule.onNodeWithText("3.2 s").performScrollTo().assertIsDisplayed()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun importSettings_updatesExpandedControlsConfigurationUi() {
        val assetManager = InstrumentationRegistry.getInstrumentation().context.assets
        val json = assetManager.open("18_mock_locations_2026_06_22_10_38_12.json").bufferedReader().use { it.readText() }
        val tempFile = File(context.cacheDir, "temp_import.json")
        tempFile.writeText(json)
        val uri = Uri.fromFile(tempFile)

        importViewModel.setImportUri(uri)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = Screen.Settings.route) {
                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = settingsViewModel, navController = navController)
                }
                composable(Screen.ImportSettings.route) {
                    ImportSettingsScreen(viewModel = importViewModel, navController = navController)
                }
                composable(Screen.ExpandedControlsConfiguration.route) {
                    ExpandedControlsConfigurationScreen(viewModel = expandedControlsConfigurationViewModel, navController = navController)
                }
            }
        }

        composeTestRule.onNodeWithText("Import settings").performScrollTo().performClick()

        composeTestRule.onNodeWithText("Import Settings").assertIsDisplayed()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            importViewModel.state.value.isImportSettingsEnabled
        }

        composeTestRule.onNodeWithText("Import").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Settings").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()

        composeTestRule.onNodeWithText("Configure expanded controls").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Expanded Controls").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Expanded Controls").assertIsDisplayed()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("mph").assertIsDisplayed()
                composeTestRule.onNodeWithText("12").assertIsDisplayed()
                composeTestRule.onNodeWithText("200").assertIsDisplayed()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }
}
