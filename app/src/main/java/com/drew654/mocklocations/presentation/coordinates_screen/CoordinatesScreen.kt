package com.drew654.mocklocations.presentation.coordinates_screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.drew654.mocklocations.domain.model.Coordinates
import com.drew654.mocklocations.presentation.MockLocationsViewModel

@Composable
fun CoordinatesScreen(
    viewModel: MockLocationsViewModel
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val clipboardManager = context.getSystemService(android.content.ClipboardManager::class.java)
    val coordinates = viewModel.coordinates.collectAsState()
    val latitude =
        remember { mutableStateOf(if (coordinates.value == null) "" else coordinates.value?.latitude.toString()) }
    val longitude =
        remember { mutableStateOf(if (coordinates.value == null) "" else coordinates.value?.longitude.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = null
            ) {
                focusManager.clearFocus()
            }
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Coordinates: ${coordinates.value}")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            val clip = clipboardManager.primaryClip
                            if (clip != null && clip.itemCount > 0) {
                                val pasteData = clip.getItemAt(0).text.toString()
                                if (pasteData.contains(",")) {
                                    val parts = pasteData.split(",")
                                    if (parts.size >= 2) {
                                        val latStr = parts[0].trim()
                                        val lonStr = parts[1].trim()

                                        val lat = latStr.toDoubleOrNull()
                                        val lon = lonStr.toDoubleOrNull()

                                        if (lat != null && lon != null &&
                                            lat in -90.0..90.0 &&
                                            lon in -180.0..180.0
                                        ) {
                                            latitude.value = latStr
                                            longitude.value = lonStr
                                            Toast.makeText(
                                                context,
                                                "Coordinates pasted",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Pasted coordinates are invalid or out of range",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Clipboard format invalid. Use 'Lat, Long'",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )
                },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = latitude.value,
                onValueChange = {
                    latitude.value = it
                },
                label = { Text("Latitude") },
                trailingIcon = {
                    if (latitude.value.isNotEmpty()) {
                        IconButton(onClick = { latitude.value = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear latitude"
                            )
                        }
                    }
                }
            )
            OutlinedTextField(
                value = longitude.value,
                onValueChange = {
                    longitude.value = it
                },
                label = { Text("Longitude") },
                trailingIcon = {
                    if (longitude.value.isNotEmpty()) {
                        IconButton(onClick = { longitude.value = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear longitude"
                            )
                        }
                    }
                }
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Button(
                onClick = {
                    val lat = latitude.value.toDoubleOrNull()
                    val long = longitude.value.toDoubleOrNull()
                    if (
                        lat != null && long != null
                        && lat in -90.0..90.0
                        && long in -180.0..180.0
                    ) {
                        viewModel.setCoordinates(
                            Coordinates(
                                latitude = latitude.value.toDouble(),
                                longitude = longitude.value.toDouble()
                            )
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "Invalid coordinates",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Text("Set Coordinates")
            }
        }
    }
}
