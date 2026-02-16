package com.drew654.mocklocations.presentation.settings_screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.drew654.mocklocations.domain.model.SpeedUnit

@Composable
fun SpeedUnitDialog(
    isVisible: Boolean,
    selectedUnit: SpeedUnit,
    onUnitSelected: (SpeedUnit) -> Unit,
    onDismiss: () -> Unit
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
                                    onUnitSelected(it)
                                    onDismiss()
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = it == selectedUnit,
                                onClick = {
                                    onUnitSelected(it)
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
