package com.drew654.mocklocations

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import com.drew654.mocklocations.presentation.Screen
import com.drew654.mocklocations.presentation.expanded_controls_configuration.ExpandedControlsConfigurationScreen
import com.drew654.mocklocations.presentation.export_settings.ExportSettingsScreen
import com.drew654.mocklocations.presentation.map_screen.MapScreen
import com.drew654.mocklocations.presentation.settings_screen.SettingsScreen
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MockLocationsViewModel by viewModels<MockLocationsViewModel>()
            val navController = rememberNavController()

            MockLocationsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Map.route,
                        modifier = Modifier.padding(innerPadding),
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                        popEnterTransition = { EnterTransition.None },
                        popExitTransition = { ExitTransition.None }
                    ) {
                        composable(Screen.Map.route) {
                            MapScreen(viewModel = viewModel, navController = navController)
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(viewModel = viewModel, navController = navController)
                        }
                        composable(Screen.ExpandedControlsConfiguration.route) {
                            ExpandedControlsConfigurationScreen(
                                viewModel = viewModel,
                                navController = navController
                            )
                        }
                        composable(Screen.ExportSettings.route) {
                            ExportSettingsScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
