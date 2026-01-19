package com.drew654.mocklocations.presentation.map_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.drew654.mocklocations.R
import com.drew654.mocklocations.presentation.Screen
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme
import com.google.maps.android.compose.CameraPositionState

@Composable
fun MapControlButtons(
    navController: NavController,
    cameraPositionState: CameraPositionState,
    controlsAreExpanded: Boolean,
    setControlsAreExpanded: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.displayCutout.only(
                    WindowInsetsSides.Horizontal
                )
            )
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            MyLocationButton(
                scope = scope,
                context = context,
                cameraPositionState = cameraPositionState
            )
            if (cameraPositionState.position.bearing != 0f) {
                CompassButton(
                    scope = scope,
                    cameraPositionState = cameraPositionState
                )
            }
        }

        MapZoomButtons(
            cameraPositionState = cameraPositionState,
            scope = scope,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .padding(bottom = 32.dp)
        )

        SmallFloatingActionButton(
            onClick = {
                navController.navigate(Screen.Settings.route)
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_settings_24),
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        ExpandControlsButton(
            onClick = {
                setControlsAreExpanded(!controlsAreExpanded)
            },
            controlsAreExpanded = controlsAreExpanded,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        )
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true
)
@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
fun MapControlButtonsPreview() {
    MockLocationsTheme {
        Surface {
            MapControlButtons(
                navController = NavController(LocalContext.current),
                cameraPositionState = CameraPositionState(),
                controlsAreExpanded = false,
                setControlsAreExpanded = { }
            )
        }
    }
}
