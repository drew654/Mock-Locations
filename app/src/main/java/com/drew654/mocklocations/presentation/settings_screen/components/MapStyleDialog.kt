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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.drew654.mocklocations.domain.model.MapStyle
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

@Composable
fun MapStyleDialog(
    isVisible: Boolean,
    selectedStyle: MapStyle?,
    onStyleSelected: (MapStyle?) -> Unit,
    onDismiss: () -> Unit
) {
    val mapStyles = listOf(
        MapStyle.Aubergine,
        MapStyle.Dark,
        MapStyle.Night,
        MapStyle.Retro,
        MapStyle.Silver,
        MapStyle.Standard
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onStyleSelected(null)
                                onDismiss()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedStyle == null,
                            onClick = {
                                onStyleSelected(null)
                                onDismiss()
                            }
                        )
                        Text(text = "Default")
                    }
                    mapStyles.forEach {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onStyleSelected(it)
                                    onDismiss()
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = it == selectedStyle,
                                onClick = {
                                    onStyleSelected(it)
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
    showBackground = true
)
@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun MapStyleDialogPreview() {
    MockLocationsTheme {
        Surface {
            MapStyleDialog(
                isVisible = true,
                selectedStyle = MapStyle.Standard,
                onStyleSelected = { },
                onDismiss = { }
            )
        }
    }
}
