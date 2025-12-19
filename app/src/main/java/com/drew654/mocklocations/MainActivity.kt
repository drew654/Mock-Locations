package com.drew654.mocklocations

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.drew654.mocklocations.presentation.Screen
import com.drew654.mocklocations.presentation.coordinates_screen.CoordinatesScreen
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            MockLocationsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Coordinates.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Coordinates.route) {
                            CoordinatesScreen()
                        }
                    }
                }
            }
        }
    }
}
