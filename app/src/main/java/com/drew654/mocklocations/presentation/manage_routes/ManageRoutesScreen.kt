package com.drew654.mocklocations.presentation.manage_routes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.ManageRoutesState
import com.drew654.mocklocations.domain.model.RouteSegment
import com.drew654.mocklocations.presentation.manage_routes.components.RouteListItem
import com.drew654.mocklocations.presentation.ui.theme.DayNightDevicePreviews
import com.drew654.mocklocations.presentation.ui.theme.DeviceThemePreview
import com.google.android.gms.maps.model.LatLng

@Composable
fun ManageRoutesScreen(
    viewModel: ManageRoutesViewModel = hiltViewModel(),
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    ManageRoutesContent(
        state = state,
        onBackButtonClicked = {
            navController.popBackStack()
        },
        onRouteSelected = { name ->
            viewModel.setSelectedRoute(name)
        },
        onRouteDeselected = {
            viewModel.deselectRoute()
        },
        onRouteMovedUp = {
            viewModel.moveRouteUp()
        },
        onRouteMovedDown = {
            viewModel.moveRouteDown()
        },
        onCopyRoute = {
            viewModel.copyRoute()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ManageRoutesContent(
    state: ManageRoutesState,
    onBackButtonClicked: () -> Unit = { },
    onRouteSelected: (String) -> Unit = { },
    onRouteDeselected: () -> Unit = { },
    onRouteMovedUp: () -> Unit = { },
    onRouteMovedDown: () -> Unit = { },
    onCopyRoute: () -> Unit = { }
) {
    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.displayCutout.only(
                    WindowInsetsSides.Horizontal
                )
            ),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Routes") },
                navigationIcon = {
                    IconButton(
                        onClick = onBackButtonClicked
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                            contentDescription = "Back"
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = state.routes,
                key = { it.name }
            ) { route ->
                RouteListItem(
                    modifier = Modifier.animateItem(),
                    route = route,
                    speedUnit = state.speedUnit,
                    isExpanded = route.name == state.selectedRouteName,
                    onClick = {
                        if (route.name == state.selectedRouteName) {
                            onRouteDeselected()
                        } else {
                            onRouteSelected(route.name)
                        }
                    },
                    onUpClicked = {
                        onRouteMovedUp()
                    },
                    onDownClicked = {
                        onRouteMovedDown()
                    },
                    onCopyClicked = {
                        onCopyRoute()
                    }
                )
            }
        }
    }
}

@DayNightDevicePreviews
@Composable
private fun ManageRoutesScreenPreview() {
    DeviceThemePreview {
        ManageRoutesContent(
            state = ManageRoutesState(
                routes = listOf(
                    LocationTarget.SavedRoute(
                        name = "Route 1",
                        routeSegments = listOf(
                            RouteSegment(
                                points = listOf(
                                    LatLng(0.0, 0.0),
                                    LatLng(0.0, 0.1)
                                )
                            )
                        )
                    ),
                    LocationTarget.SavedRoute(
                        name = "Route 2",
                        routeSegments = listOf(
                            RouteSegment(
                                points = listOf(
                                    LatLng(0.0, 0.0),
                                    LatLng(0.0, 0.12),
                                    LatLng(0.0, 0.08)
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}
