package com.drew654.mocklocations.presentation.expanded_controls_configuration

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.ExpandedControlsConfigurationState
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import com.drew654.mocklocations.presentation.settings_screen.components.SpeedUnitDialog
import com.drew654.mocklocations.presentation.settings_screen.components.TextRow
import com.drew654.mocklocations.presentation.ui.theme.DayNightDevicePreviews
import com.drew654.mocklocations.presentation.ui.theme.DeviceThemePreview

@Composable
fun ExpandedControlsConfigurationScreen(
    viewModel: ExpandedControlsConfigurationViewModel = hiltViewModel(),
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    ExpandedControlsConfigurationContent(
        state = state,
        onSave = {
            viewModel.save {
                navController.popBackStack()
            }
        },
        onBack = {
            navController.popBackStack()
        },
        setIsShowingDialog = { newValue ->
            viewModel.setIsShowingDialog(newValue)
        },
        setSpeedSliderLowerEnd = { newValue ->
            viewModel.setSpeedSliderLowerEnd(newValue)
        },
        setSpeedSliderUpperEnd = { newValue ->
            viewModel.setSpeedSliderUpperEnd(newValue)
        },
        setSpeedUnitValue = { newValue ->
            viewModel.setSpeedUnitValue(newValue)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExpandedControlsConfigurationContent(
    state: ExpandedControlsConfigurationState,
    onSave: () -> Unit = { },
    onBack: () -> Unit = { },
    setIsShowingDialog: (Boolean) -> Unit = { },
    setSpeedSliderLowerEnd: (String) -> Unit = { },
    setSpeedSliderUpperEnd: (String) -> Unit = { },
    setSpeedUnitValue: (SpeedUnitValue) -> Unit = { }
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val speedSliderLowerEnd = state.speedSliderLowerEnd
    val speedSliderUpperEnd = state.speedSliderUpperEnd
    val isShowingDialog = state.isShowingDialog
    val speedUnitValue = state.speedUnitValue

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.displayCutout.only(
                    WindowInsetsSides.Horizontal
                )
            )
            .clickable(interactionSource = null, indication = null) {
                focusManager.clearFocus()
            },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Expanded Controls") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onBack()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                            contentDescription = "Back"
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                TextRow(
                    label = "Speed unit",
                    onClick = {
                        setIsShowingDialog(true)
                    },
                    value = speedUnitValue.speedUnit.name
                )

                OutlinedTextField(
                    value = speedSliderLowerEnd,
                    onValueChange = { setSpeedSliderLowerEnd(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    label = { Text("Lower end") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value = speedSliderUpperEnd,
                    onValueChange = { setSpeedSliderUpperEnd(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    label = { Text("Upper end") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )

                Spacer(Modifier.padding(bottom = 16.dp))
            }

            TextButton(
                onClick = {
                    if (state.isFormValid()) {
                        onSave()
                    } else {
                        Toast.makeText(context, "Invalid values", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("Save")
            }
        }
    }

    SpeedUnitDialog(
        isVisible = isShowingDialog,
        onDismiss = { setIsShowingDialog(false) },
        selectedSpeedUnitValue = speedUnitValue,
        onSpeedUnitValueSelected = {
            setSpeedUnitValue(it)
            setIsShowingDialog(false)
        }
    )
}

@DayNightDevicePreviews
@Composable
private fun ExpandableControlsConfigurationContentPreview() {
    DeviceThemePreview {
        ExpandedControlsConfigurationContent(
            state = ExpandedControlsConfigurationState(
                isShowingDialog = false,
                speedUnitValue = SpeedUnitValue(30.0, SpeedUnit.MilesPerHour),
                speedSliderLowerEnd = "0",
                speedSliderUpperEnd = "100",
            )
        )
    }
}
