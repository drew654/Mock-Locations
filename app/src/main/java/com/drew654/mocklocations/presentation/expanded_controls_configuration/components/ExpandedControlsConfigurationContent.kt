package com.drew654.mocklocations.presentation.expanded_controls_configuration.components

import android.content.res.Configuration
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import com.drew654.mocklocations.domain.model.getSpeedUnitByName
import com.drew654.mocklocations.presentation.settings_screen.components.SpeedUnitDialog
import com.drew654.mocklocations.presentation.settings_screen.components.TextRow
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

val SpeedUnitValueSaver = listSaver<SpeedUnitValue, Any>(
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
fun ExpandedControlsConfigurationContent(
    originalSpeedUnitValue: SpeedUnitValue,
    originalSpeedSliderLowerEnd: Int,
    originalSpeedSliderUpperEnd: Int,
    onSaved: (SpeedUnitValue, Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    var isShowingSpeedUnitDialog by rememberSaveable { mutableStateOf(false) }
    var speedUnitValue by rememberSaveable(originalSpeedUnitValue, stateSaver = SpeedUnitValueSaver) {
        mutableStateOf(originalSpeedUnitValue)
    }
    var speedSliderLowerEnd by rememberSaveable(originalSpeedSliderLowerEnd) {
        mutableStateOf(originalSpeedSliderLowerEnd.toString())
    }
    var speedSliderUpperEnd by rememberSaveable(originalSpeedSliderUpperEnd) {
        mutableStateOf(originalSpeedSliderUpperEnd.toString())
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
                            speedUnitValue,
                            speedSliderLowerEnd.toInt(),
                            speedSliderUpperEnd.toInt()
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
fun ExpandableControlsConfigurationContentPreview() {
    MockLocationsTheme {
        Surface {
            ExpandedControlsConfigurationContent(
                originalSpeedUnitValue = SpeedUnitValue(30.0, SpeedUnit.MilesPerHour),
                originalSpeedSliderLowerEnd = 0,
                originalSpeedSliderUpperEnd = 100,
                onSaved = { _, _, _ -> },
                onBack = { }
            )
        }
    }
}
