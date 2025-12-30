package com.drew654.mocklocations

import android.app.AppOpsManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import com.drew654.mocklocations.presentation.Screen
import com.drew654.mocklocations.presentation.components.PermissionsDialog
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
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current
            val isShowingPermissionsDialog by viewModel.isShowingPermissionsDialog.collectAsState()

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        try {
                            val opsManager = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
                            val mode = opsManager.checkOpNoThrow(
                                AppOpsManager.OPSTR_MOCK_LOCATION,
                                android.os.Process.myUid(),
                                context.packageName
                            )
                            viewModel.setIsShowingPermissionsDialog(mode != AppOpsManager.MODE_ALLOWED)
                        } catch (e: Exception) {
                            viewModel.setIsShowingPermissionsDialog(true)
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            PermissionsDialog(
                showMockLocationDialog = isShowingPermissionsDialog,
                setShowMockLocationDialog = { viewModel.setIsShowingPermissionsDialog(it) },
                context = context
            )

            MockLocationsTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("Mock Locations") },
                            navigationIcon = {
                                if (currentRoute != Screen.Map.route) {
                                    IconButton(
                                        onClick = {
                                            navController.popBackStack()
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            },
                            actions = {
                                if (currentRoute != Screen.Settings.route) {
                                    IconButton(
                                        onClick = {
                                            navController.navigate(Screen.Settings.route)
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_settings_24),
                                            contentDescription = "Settings"
                                        )
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Map.route,
                        modifier = Modifier.padding(innerPadding),
                        enterTransition = { androidx.compose.animation.EnterTransition.None },
                        exitTransition = { androidx.compose.animation.ExitTransition.None },
                        popEnterTransition = { androidx.compose.animation.EnterTransition.None },
                        popExitTransition = { androidx.compose.animation.ExitTransition.None }
                    ) {
                        composable(Screen.Map.route) {
                            MapScreen(viewModel = viewModel)
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
