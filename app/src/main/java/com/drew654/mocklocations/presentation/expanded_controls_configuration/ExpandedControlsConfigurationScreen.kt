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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.ExpandedControlsState
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import com.drew654.mocklocations.domain.model.getSpeedUnitByName
import com.drew654.mocklocations.presentation.MockLocationsViewModel
import com.drew654.mocklocations.presentation.settings_screen.components.SpeedUnitDialog
import com.drew654.mocklocations.presentation.settings_screen.components.TextRow
import com.drew654.mocklocations.presentation.ui.theme.DayNightDevicePreviews
import com.drew654.mocklocations.presentation.ui.theme.DeviceThemePreview

@Composable
fun ExpandedControlsConfigurationScreen(
    viewModel: MockLocationsViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()

    ExpandedControlsConfigurationContent(
        originalState = uiState.expandedControlsState,
        onSaved = { expandedControlsState ->
            val speedUnitValue = expandedControlsState.speedUnitValue
            val speedSliderLowerEnd = expandedControlsState.speedSliderLowerEnd
            val speedSliderUpperEnd = expandedControlsState.speedSliderUpperEnd
            viewModel.setSpeedUnitValue(speedUnitValue)
            viewModel.saveSpeedUnitValue(speedUnitValue)
            if (speedUnitValue.value < speedSliderLowerEnd) {
                viewModel.setSpeedUnitValue(speedUnitValue.copy(value = speedSliderLowerEnd.toDouble()))
                viewModel.saveSpeedUnitValue(speedUnitValue.copy(value = speedSliderLowerEnd.toDouble()))
            }
            if (speedUnitValue.value > speedSliderUpperEnd) {
                viewModel.setSpeedUnitValue(speedUnitValue.copy(value = speedSliderUpperEnd.toDouble()))
                viewModel.saveSpeedUnitValue(speedUnitValue.copy(value = speedSliderUpperEnd.toDouble()))
            }
            viewModel.setSpeedSliderLowerEnd(speedSliderLowerEnd)
            viewModel.saveSpeedSliderLowerEnd(speedSliderLowerEnd)
            viewModel.setSpeedSliderUpperEnd(speedSliderUpperEnd)
            viewModel.saveSpeedSliderUpperEnd(speedSliderUpperEnd)
            navController.popBackStack()
        },
        onBack = {
            navController.popBackStack()
        }
    )
}

private val SpeedUnitValueSaver = listSaver<SpeedUnitValue, Any>(
    save = { listOf(it.value, it.speedUnit.name) },
    restore = {
        SpeedUnitValue(
            value = it[0] as Double,
            speedUnit = getSpeedUnitByName(it[1] as String)
        )
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedControlsConfigurationContent(
    originalState: ExpandedControlsState = ExpandedControlsState(),
    onSaved: (ExpandedControlsState) -> Unit = { },
    onBack: () -> Unit = { }
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    var isShowingSpeedUnitDialog by rememberSaveable { mutableStateOf(false) }
    var speedUnitValue by rememberSaveable(originalState.speedUnitValue, stateSaver = SpeedUnitValueSaver) {
        mutableStateOf(originalState.speedUnitValue)
    }
    var speedSliderLowerEnd by rememberSaveable(originalState.speedSliderLowerEnd) {
        mutableStateOf(originalState.speedSliderLowerEnd.toString())
    }
    var speedSliderUpperEnd by rememberSaveable(originalState.speedSliderUpperEnd) {
        mutableStateOf(originalState.speedSliderUpperEnd.toString())
    }

    fun formIsValid(): Boolean {
        return !(
                speedSliderLowerEnd.toIntOrNull() == null
                        || speedSliderUpperEnd.toIntOrNull() == null
                        || speedSliderLowerEnd.toInt() >= speedSliderUpperEnd.toInt()
                        || speedSliderLowerEnd.toInt() < 0
                )
    }

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
                        isShowingSpeedUnitDialog = true
                    },
                    value = speedUnitValue.speedUnit.name
                )

                OutlinedTextField(
                    value = speedSliderLowerEnd,
                    onValueChange = { speedSliderLowerEnd = it },
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
                    onValueChange = { speedSliderUpperEnd = it },
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
                    if (formIsValid()) {
                        onSaved(
                            ExpandedControlsState(
                                speedUnitValue = speedUnitValue,
                                speedSliderLowerEnd = speedSliderLowerEnd.toInt(),
                                speedSliderUpperEnd = speedSliderUpperEnd.toInt()
                            )
                        )
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
        isVisible = isShowingSpeedUnitDialog,
        onDismiss = { isShowingSpeedUnitDialog = false },
        selectedSpeedUnitValue = speedUnitValue,
        onSpeedUnitValueSelected = {
            speedUnitValue = it
            isShowingSpeedUnitDialog = false
        }
    )
}

@DayNightDevicePreviews
@Composable
private fun ExpandableControlsConfigurationContentPreview() {
    DeviceThemePreview {
        ExpandedControlsConfigurationContent()
    }
}
