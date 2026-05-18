package com.drew654.mocklocations.presentation.settings_screen.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

@Composable
fun SpeedUnitDialog(
    isVisible: Boolean,
    selectedSpeedUnitValue: SpeedUnitValue,
    onSpeedUnitValueSelected: (SpeedUnitValue) -> Unit = { },
    onDismiss: () -> Unit = { }
) {
    val speedUnits = listOf(
        SpeedUnit.KilometersPerHour,
        SpeedUnit.MetersPerSecond,
        SpeedUnit.MilesPerHour
    )

    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss
        ) {
            Card {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    speedUnits.forEach {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onSpeedUnitValueSelected(selectedSpeedUnitValue.copy(speedUnit = it))
                                    onDismiss()
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = it == selectedSpeedUnitValue.speedUnit,
                                onClick = {
                                    onSpeedUnitValueSelected(selectedSpeedUnitValue.copy(speedUnit = it))
                                    onDismiss()
                                }
                            )
                            Text(text = it.name)
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    showSystemUi = true
)
@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun SpeedUnitDialogPhonePreview() {
    MockLocationsTheme {
        Scaffold { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                SpeedUnitDialog(
                    isVisible = true,
                    selectedSpeedUnitValue = SpeedUnitValue(30.0, SpeedUnit.MilesPerHour)
                )
            }
        }
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    showSystemUi = true,
    device = Devices.TABLET
)
@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    showSystemUi = true,
    device = Devices.TABLET
)
@Composable
private fun SpeedUnitDialogTabletPreview() {
    MockLocationsTheme {
        Scaffold { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                SpeedUnitDialog(
                    isVisible = true,
                    selectedSpeedUnitValue = SpeedUnitValue(30.0, SpeedUnit.MilesPerHour)
                )
            }
        }
    }
}
