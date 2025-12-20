package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.drew654.mocklocations.presentation.NoRippleInteractionSource

@Composable
fun DisableableFloatingActionButton(
    onClick: () -> Unit,
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = {
            if (enabled) {
                onClick()
            }
        },
        containerColor = if (enabled)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        interactionSource = remember(enabled) { if (enabled) MutableInteractionSource() else NoRippleInteractionSource() }
    ) {
        content()
    }
}
