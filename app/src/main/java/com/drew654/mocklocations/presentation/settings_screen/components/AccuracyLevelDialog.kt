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
import com.drew654.mocklocations.domain.model.AccuracyLevel
import com.drew654.mocklocations.presentation.ui.theme.MockLocationsTheme

@Composable
fun AccuracyLevelDialog(
    isVisible: Boolean,
    selectedLevel: AccuracyLevel,
    onLevelSelected: (AccuracyLevel) -> Unit,
    onDismiss: () -> Unit
) {
    val accuracyLevels = listOf(
        AccuracyLevel.Perfect,
        AccuracyLevel.High,
        AccuracyLevel.Medium,
        AccuracyLevel.Low
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
                    accuracyLevels.forEach {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onLevelSelected(it)
                                    onDismiss()
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = it == selectedLevel,
                                onClick = {
                                    onLevelSelected(it)
                                    onDismiss()
                                }
                            )
                            Text(text = "${it.name} (${it.meters.toInt()} m)")
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
private fun AccuracyLevelDialogPreview() {
    MockLocationsTheme {
        Surface {
            AccuracyLevelDialog(
                isVisible = true,
                selectedLevel = AccuracyLevel.Perfect,
                onLevelSelected = { },
                onDismiss = { }
            )
        }
    }
}
